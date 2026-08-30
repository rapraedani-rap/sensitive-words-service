package za.co.flash.sensitivewords.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanitizationRequest {

    @NotBlank(message = "Text is required")
    private String text;
}