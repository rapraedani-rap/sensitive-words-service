package za.co.flash.sensitivewords.service.strategy;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;
import za.co.flash.sensitivewords.enums.FileType;
import za.co.flash.sensitivewords.enums.InputType;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TxtSensitiveWordInputStrategy implements SensitiveWordInputStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(SensitiveWordInputRequest request) {

        return request.getInputType() == InputType.FILE && request.getFileType() == FileType.TXT;
    }

    @Override
    public List<String> extractWords(SensitiveWordInputRequest request) {

        try {

            return objectMapper.readValue(request.getFile().getInputStream(),
                    new TypeReference<List<String>>() {});

        } catch (IOException e) {

            throw new IllegalArgumentException("Unable to read sensitive words TXT file", e);
        }
    }
}