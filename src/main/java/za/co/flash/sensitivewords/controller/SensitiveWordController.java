package za.co.flash.sensitivewords.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordInputResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.enums.FileType;
import za.co.flash.sensitivewords.enums.InputType;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;
import za.co.flash.sensitivewords.service.SensitiveWordInputService;
import za.co.flash.sensitivewords.service.SensitiveWordService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sensitive-words")
@RequiredArgsConstructor
@Tag(name = "Sensitive Word Management", description = "Internal APIs for managing sensitive words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;
    private final SensitiveWordInputService sensitiveWordInputService;
    private final SensitiveWordCacheService sensitiveWordCacheService;


    // =========================================================
    // JSON INPUT
    // =========================================================

    @PostMapping("/add-from-json")
    @Operation(
            summary = "Add sensitive words using JSON",
            description = """
                    Adds one or multiple sensitive words using JSON.

                    Example:
                    {
                      "words": [
                        "PASSWORD",
                        "CREATE",
                        "SELECT * FROM"
                      ]
                    }
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sensitive words processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<SensitiveWordInputResponse> addFromJson(
            @Valid @RequestBody SensitiveWordInputRequest request, Principal principal) {


        SensitiveWordInputResponse response = sensitiveWordInputService.process(
                request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // =========================================================
    // FILE INPUT
    // =========================================================

    @PostMapping(value = "/add-from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Add sensitive words from a file",
            description = "Uploads sensitive words using a supported file format. Supported file types: TXT and CSV."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or unsupported file")
    })
    public ResponseEntity<SensitiveWordInputResponse> addFromFile(
            @Parameter(description = "File containing sensitive words", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Format of the uploaded file", example = "TXT", required = true)
            @RequestParam FileType fileType, Principal principal) {

        SensitiveWordInputRequest request = SensitiveWordInputRequest.builder()
                .inputType(InputType.FILE)
                .fileType(fileType)
                .file(file)
                .build();

        SensitiveWordInputResponse response = sensitiveWordInputService.process(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // =========================================================
    // READ
    // =========================================================

    @GetMapping("/get-by-status")
    @Operation(summary = "Get sensitive words",
            description = "Returns a paginated list of sensitive words. Optionally filter by active status."
    )
    public ResponseEntity<Page<SensitiveWord>> getAll(@RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return ResponseEntity.ok(sensitiveWordService.findAll(active, pageable));
    }


    @GetMapping("/get-word")
    @Operation(summary = "Get sensitive word by word or phrase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensitive word found"),
            @ApiResponse(responseCode = "400", description = "Sensitive word not found")
    })
    public ResponseEntity<SensitiveWord> getByWord(@RequestParam String word) {

        return ResponseEntity.ok(sensitiveWordService.findByWord(word));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/update-word")
    @Operation(summary = "Update a sensitive word")
    public ResponseEntity<SensitiveWord> update(@RequestParam String word, @RequestParam String newWord,
            Principal principal) {

        return ResponseEntity.ok(sensitiveWordService.update(word, newWord, principal.getName()));
    }


    // =========================================================
    // DISABLE / DELETE
    // =========================================================

    @DeleteMapping("/disable-word")
    @Operation(summary = "Disable a sensitive word",
            description = "Performs a soft delete by disabling the sensitive word. The record is retained for auditing purposes."
    )
    public ResponseEntity<Void> disable(@RequestParam String word, Principal principal) {

        sensitiveWordService.disable(word, principal.getName());

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // ENABLE
    // =========================================================

    @PatchMapping("/enable-word")
    @Operation(summary = "Enable a sensitive word")
    public ResponseEntity<SensitiveWord> enable(@RequestParam String word, Principal principal) {

        return ResponseEntity.ok(sensitiveWordService.enable(word, principal.getName()));
    }


    // =========================================================
    // CACHE
    // =========================================================

    @PostMapping("/refresh-cache")
    @Operation(summary = "Refresh sensitive words cache",
            description = "Reloads all active sensitive words from the database into the application cache."
    )
    public ResponseEntity<Map<String, String>> refreshCache() {

        sensitiveWordCacheService.refresh();

        return ResponseEntity.ok(Map.of("message", "Sensitive words " +
                "cache refreshed successfully"));
    }
}