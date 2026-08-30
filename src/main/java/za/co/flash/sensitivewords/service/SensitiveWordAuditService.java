package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.entity.SensitiveWord;

public interface SensitiveWordAuditService {

    void recordCreate(SensitiveWord word, String changedBy);

    void recordUpdate(SensitiveWord word, String oldValue, String newValue, String changedBy);

    void recordDisable(SensitiveWord word, String changedBy);

    void recordEnable(SensitiveWord word, String changedBy);
}