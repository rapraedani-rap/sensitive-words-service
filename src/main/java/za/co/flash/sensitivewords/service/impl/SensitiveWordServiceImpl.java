package za.co.flash.sensitivewords.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.entity.SensitiveWordUsage;
import za.co.flash.sensitivewords.exception.SensitiveWordException;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import za.co.flash.sensitivewords.repository.SensitiveWordUsageRepository;
import za.co.flash.sensitivewords.service.SensitiveWordAuditService;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;
import za.co.flash.sensitivewords.service.SensitiveWordService;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;
    private final SensitiveWordUsageRepository usageRepository;
    private final SensitiveWordAuditService auditService;
    private final SensitiveWordCacheService sensitiveWordCacheService;

    @Override
    @Transactional
    public SensitiveWord create(String word, final String changedBy) {

        String normalizedWord = normalize(word);

        log.info("Creating sensitive word. word={}", normalizedWord);

        if (sensitiveWordRepository.existsByWordIgnoreCase(normalizedWord)) {

            log.warn("Sensitive word already exists. word={}", normalizedWord);

            throw new SensitiveWordException("Sensitive word already exists: " + normalizedWord);
        }

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .word(normalizedWord)
                .active(true)
                .build();

        SensitiveWord saved = sensitiveWordRepository.save(sensitiveWord);

        usageRepository.save(SensitiveWordUsage.builder().sensitiveWord(saved)
                .usageCount(0L).build());

        auditService.recordCreate(saved, changedBy);

        sensitiveWordCacheService.refresh();

        log.info("Sensitive word created successfully. id={}, word={}, changedBy={}",
                saved.getId(), saved.getWord(), changedBy);

        return saved;
    }

    @Transactional(readOnly = true)
    @Override
    public SensitiveWord findByWord(String word) {

        log.debug("Finding sensitive word. word={}", word);

        return sensitiveWordRepository.findByWordIgnoreCase(word).orElseThrow(() -> {

            log.warn("Sensitive word not found. word={}", word);

            return new SensitiveWordException("Sensitive word not found: " + word);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SensitiveWord> findAll(Boolean active, Pageable pageable) {

        log.info("Finding sensitive words. active={}, page={}, size={}",
                active, pageable.getPageNumber(), pageable.getPageSize());

        return sensitiveWordRepository.findAllByActive(active, pageable);
    }

    @Override
    @Transactional
    public SensitiveWord update(String word, String newWord, final String changedBy) {

        log.info("Updating sensitive word. word={}, newWord={}", word, newWord);

        SensitiveWord sensitiveWord = findByWord(word);

        String oldValue = sensitiveWord.getWord();
        String normalizedNewWord = normalize(newWord);

        sensitiveWordRepository.findByWordIgnoreCase(normalizedNewWord)
                .filter(existing -> !existing.getId().equals(sensitiveWord.getId()))
                .ifPresent(existing -> {

                    log.info("Sensitive word already exists. word={}", normalizedNewWord);

                    throw new SensitiveWordException("Sensitive word already exists: " + normalizedNewWord);
                });

        sensitiveWord.setWord(normalizedNewWord);

        SensitiveWord updated = sensitiveWordRepository.save(sensitiveWord);

        auditService.recordUpdate(updated, oldValue, normalizedNewWord, changedBy);

        sensitiveWordCacheService.refresh();

        log.info("Sensitive word updated successfully. id={}, oldValue={}, newValue={}, changedBy={}",
                updated.getId(), oldValue, normalizedNewWord, changedBy);

        return updated;
    }

    @Override
    @Transactional
    public SensitiveWord disable(String word,final String changedBy) {

        log.info("Disabling sensitive word. word={}", word);

        SensitiveWord sensitiveWord = findByWord(word);

        if (!Boolean.TRUE.equals(sensitiveWord.getActive())) {

            log.info("Sensitive word already disabled. id={}, word={}",
                    sensitiveWord.getId(), sensitiveWord.getWord());

            return sensitiveWord;
        }

        sensitiveWord.setActive(false);

        SensitiveWord updated = sensitiveWordRepository.save(sensitiveWord);

        auditService.recordDisable(updated, changedBy);

        sensitiveWordCacheService.refresh();

        log.info("Sensitive word disabled successfully. id={}, word={}, changedBy={}",
                updated.getId(), updated.getWord(), changedBy);

        return updated;
    }

    @Override
    @Transactional
    public SensitiveWord enable(String word, final String changedBy) {

        log.info("Enabling sensitive word. word={}", word);

        SensitiveWord sensitiveWord = findByWord(word);

        if (Boolean.TRUE.equals(sensitiveWord.getActive())) {

            log.info("Sensitive word already enabled. id={}, word={}",
                    sensitiveWord.getId(), sensitiveWord.getWord());

            return sensitiveWord;
        }

        sensitiveWord.setActive(true);

        SensitiveWord updated = sensitiveWordRepository.save(sensitiveWord);

        auditService.recordEnable(updated, changedBy);

        sensitiveWordCacheService.refresh();

        log.info("Sensitive word enabled successfully. id={}, word={}, changedBy={}",
                updated.getId(), updated.getWord(), changedBy);

        return updated;
    }

    private String normalize(String word) {

        if (word == null || word.isBlank()) {
            throw new SensitiveWordException("Sensitive word is required");
        }

        String normalizedWord = word.trim().toUpperCase(Locale.ROOT);

        if (normalizedWord.length() > 255) {
            throw new SensitiveWordException("Sensitive word cannot exceed 255 characters");
        }

        return normalizedWord;
    }
}