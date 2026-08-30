package za.co.flash.sensitivewords.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordInputResponse {

    private int totalReceived;
    private int inserted;
    private int duplicates;
    private int invalid;

    private List<String> duplicateWords;
    private List<String> invalidWords;
}