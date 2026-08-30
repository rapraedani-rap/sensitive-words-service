package za.co.flash.sensitivewords.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import za.co.flash.sensitivewords.enums.FileType;
import za.co.flash.sensitivewords.enums.InputType;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordInputRequest {

    private InputType inputType;

    private FileType fileType;

    private List<String> words;

    @JsonIgnore
    private MultipartFile file;
}