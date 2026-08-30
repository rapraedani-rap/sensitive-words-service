package za.co.flash.sensitivewords.service.strategy;


import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;

import java.util.List;

public interface SensitiveWordInputStrategy {

    boolean supports(SensitiveWordInputRequest request);

    List<String> extractWords(SensitiveWordInputRequest request);
}