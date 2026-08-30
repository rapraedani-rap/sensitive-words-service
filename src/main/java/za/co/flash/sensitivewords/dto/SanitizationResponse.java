package za.co.flash.sensitivewords.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SanitizationResponse {

    private String sanitizedText;
}