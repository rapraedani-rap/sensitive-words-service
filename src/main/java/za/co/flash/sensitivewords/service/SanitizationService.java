package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.dto.SanitizationResponse;

public interface SanitizationService {

    SanitizationResponse sanitize(String text);
}