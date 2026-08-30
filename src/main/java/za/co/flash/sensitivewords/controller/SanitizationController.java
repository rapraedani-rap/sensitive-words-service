package za.co.flash.sensitivewords.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.flash.sensitivewords.dto.SanitizationRequest;
import za.co.flash.sensitivewords.dto.SanitizationResponse;
import za.co.flash.sensitivewords.service.SanitizationService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SanitizationController {

    private final SanitizationService sanitizationService;


    // =========================================================
    // SANITIZE
    // =========================================================

    @PostMapping("/sanitize")
    @Operation(summary = "Sanitize text",
            description = "Replaces configured sensitive words or phrases with asterisks"
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Text sanitized successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<SanitizationResponse> sanitize(@Valid @RequestBody SanitizationRequest request) {

        return ResponseEntity.ok(sanitizationService.sanitize(request.getText())
        );
    }
}