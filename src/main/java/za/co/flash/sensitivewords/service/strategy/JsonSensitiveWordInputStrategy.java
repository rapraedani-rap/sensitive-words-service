package za.co.flash.sensitivewords.service.strategy;

import org.springframework.stereotype.Component;
import za.co.flash.sensitivewords.enums.InputType;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;

import java.util.List;

@Component
public class JsonSensitiveWordInputStrategy implements SensitiveWordInputStrategy {

    @Override
    public boolean supports(SensitiveWordInputRequest request) {
        return request.getInputType() == InputType.JSON;
    }

    @Override
    public List<String> extractWords(
            SensitiveWordInputRequest request
    ) {
        return request.getWords();
    }
}