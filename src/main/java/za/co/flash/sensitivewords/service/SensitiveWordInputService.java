package za.co.flash.sensitivewords.service;

import org.springframework.transaction.annotation.Transactional;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordInputResponse;

public interface SensitiveWordInputService {
    @Transactional
    SensitiveWordInputResponse process(SensitiveWordInputRequest request, final String changedBy);
}
