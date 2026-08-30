package za.co.flash.sensitivewords.service.strategy;

import org.springframework.stereotype.Component;
import za.co.flash.sensitivewords.enums.FileType;
import za.co.flash.sensitivewords.enums.InputType;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;

import java.util.List;

@Component
public class CsvSensitiveWordStrategy implements SensitiveWordInputStrategy {

    @Override
    public boolean supports(SensitiveWordInputRequest request) {
        return request.getInputType() == InputType.FILE
                && request.getFileType() == FileType.CSV;
    }

    @Override
    public List<String> extractWords(SensitiveWordInputRequest request) {
        // Parse using Apache Commons CSV
        return null;
    }
}