package za.co.flash.sensitivewords.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.flash.sensitivewords.dto.SanitizationResponse;
import za.co.flash.sensitivewords.model.SensitiveWordRule;
import za.co.flash.sensitivewords.service.SanitizationService;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;

import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class SanitizationServiceImpl implements SanitizationService {

    private final SensitiveWordCacheService sensitiveWordCacheService;


    @Override
    public SanitizationResponse sanitize(String text) {

        String sanitizedText = text;

        for (SensitiveWordRule rule : sensitiveWordCacheService.getRules()) {

            sanitizedText = rule.getPattern().matcher(sanitizedText).replaceAll(
                    Matcher.quoteReplacement("*".repeat(rule.getWord().length())));
        }

        return SanitizationResponse.builder().sanitizedText(sanitizedText)
                .build();
    }
}