package za.co.flash.sensitivewords.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.entity.SensitiveWordAudit;
import za.co.flash.sensitivewords.enums.AuditAction;
import za.co.flash.sensitivewords.repository.SensitiveWordAuditRepository;
import za.co.flash.sensitivewords.service.SensitiveWordAuditService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SensitiveWordAuditServiceImpl implements SensitiveWordAuditService {

    private final SensitiveWordAuditRepository auditRepository;

    @Override
    public void recordCreate(SensitiveWord word, String changedBy) {
        save(word, AuditAction.CREATE, null, word.getWord(), changedBy);
    }

    @Override
    public void recordUpdate(SensitiveWord word, String oldValue, String newValue, String changedBy) {
        save(word, AuditAction.UPDATE, oldValue, newValue, changedBy);
    }

    @Override
    public void recordDisable(SensitiveWord word, String changedBy) {
        save(word, AuditAction.DISABLE, word.getWord(), word.getWord(), changedBy);
    }

    @Override
    public void recordEnable(SensitiveWord word, String changedBy) {
        save(word, AuditAction.ENABLE, word.getWord(), word.getWord(), changedBy);
    }

    private void save(SensitiveWord word, AuditAction action, String oldValue,
            String newValue, String changedBy) {

        auditRepository.save(SensitiveWordAudit.builder().sensitiveWord(word)
                        .action(action).oldValue(oldValue).newValue(newValue)
                        .changedAt(LocalDateTime.now())
                        .changedBy(changedBy).build());
    }
}