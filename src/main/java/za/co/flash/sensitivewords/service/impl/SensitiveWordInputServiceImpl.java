package za.co.flash.sensitivewords.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;
import za.co.flash.sensitivewords.dto.SensitiveWordInputResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.entity.SensitiveWordAudit;
import za.co.flash.sensitivewords.entity.SensitiveWordUsage;
import za.co.flash.sensitivewords.enums.AuditAction;
import za.co.flash.sensitivewords.repository.SensitiveWordAuditRepository;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import za.co.flash.sensitivewords.repository.SensitiveWordUsageRepository;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;
import za.co.flash.sensitivewords.service.SensitiveWordInputService;
import za.co.flash.sensitivewords.service.strategy.SensitiveWordInputStrategy;
import za.co.flash.sensitivewords.service.strategy.SensitiveWordInputStrategyResolver;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordInputServiceImpl implements SensitiveWordInputService {

    private final SensitiveWordInputStrategyResolver strategyResolver;

    private final SensitiveWordRepository sensitiveWordRepository;
    private final SensitiveWordAuditRepository auditRepository;
    private final SensitiveWordUsageRepository usageRepository;
    private final SensitiveWordCacheService sensitiveWordCacheService;

    @Transactional
    @Override
    public SensitiveWordInputResponse process(SensitiveWordInputRequest request, final String changedBy) {


        log.info("Processing sensitive word input. inputType={}, fileType={}, changedBy={}",
                request.getInputType(), request.getFileType(), changedBy);

        SensitiveWordInputStrategy strategy = strategyResolver.resolve(request);

        List<String> rawWords = strategy.extractWords(request);

        log.info("Sensitive words extracted. totalReceived={}",
                rawWords == null ? 0 : rawWords.size());

        return processWords(rawWords, changedBy);
    }


    private SensitiveWordInputResponse processWords(List<String> rawWords, String changedBy) {

        if (rawWords == null || rawWords.isEmpty()) {

            log.warn("No sensitive words were supplied. changedBy={}", changedBy);

            throw new IllegalArgumentException("No sensitive words were supplied");
        }

        int totalReceived = rawWords.size();

        List<String> invalidWords = new ArrayList<>();
        List<String> duplicateWords = new ArrayList<>();


        // =====================================================
        // NORMALIZE + VALIDATE + REMOVE REQUEST DUPLICATES
        // =====================================================

        Set<String> uniqueWords = new LinkedHashSet<>();

        for (String rawWord : rawWords) {

            String normalized = normalize(rawWord);

            if (!isValid(normalized)) {

                log.info("Invalid sensitive word found. word={}", rawWord);

                invalidWords.add(rawWord);
                continue;
            }

            if (!uniqueWords.add(normalized)) {

                log.info("Duplicate sensitive word found in request. word={}", normalized);

                duplicateWords.add(normalized);
            }
        }

        log.info("Sensitive words validated. unique={}, duplicates={}, invalid={}",
                uniqueWords.size(), duplicateWords.size(), invalidWords.size());


        // =====================================================
        // GET EXISTING WORDS FROM DB
        // =====================================================

        Set<String> existingWords = uniqueWords.isEmpty() ? Collections.emptySet() :
                new HashSet<>(sensitiveWordRepository.findExistingWords(uniqueWords));

        log.info("Existing sensitive words found. count={}", existingWords.size());


        // =====================================================
        // REMOVE DATABASE DUPLICATES
        // =====================================================

        List<String> newWords = new ArrayList<>();

        for (String word : uniqueWords) {

            if (existingWords.contains(word)) {

                log.info("Sensitive word already exists in database. word={}", word);

                duplicateWords.add(word);

            } else {

                newWords.add(word);
            }
        }


        // =====================================================
        // CREATE ENTITIES
        // =====================================================

        List<SensitiveWord> entities = newWords.stream()
                .map(word -> SensitiveWord.builder()
                        .word(word)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();


        // =====================================================
        // SAVE SENSITIVE WORDS
        // =====================================================

        List<SensitiveWord> savedWords = sensitiveWordRepository.saveAll(entities);

        sensitiveWordRepository.flush();

        log.info("Sensitive words saved. count={}", savedWords.size());


        // =====================================================
        // CREATE USAGE RECORDS
        // =====================================================

        List<SensitiveWordUsage> usageRecords = savedWords.stream()
                .map(word -> SensitiveWordUsage.builder()
                        .sensitiveWord(word)
                        .usageCount(0L)
                        .build())
                .toList();

        usageRepository.saveAll(usageRecords);

        log.info("Sensitive word usage records created. count={}", usageRecords.size());


        // =====================================================
        // CREATE AUDIT RECORDS
        // =====================================================

        List<SensitiveWordAudit> auditRecords = savedWords.stream()
                .map(word -> SensitiveWordAudit.builder()
                        .sensitiveWord(word)
                        .action(AuditAction.CREATE)
                        .changedAt(LocalDateTime.now())
                        .oldValue(null)
                        .newValue(word.getWord())
                        .changedBy(changedBy.trim())
                        .build())
                .toList();

        auditRepository.saveAll(auditRecords);

        log.info("Sensitive word audit records created. count={}", auditRecords.size());


        // =====================================================
        // REFRESH CACHE
        // =====================================================

        if (!savedWords.isEmpty()) {

            sensitiveWordCacheService.refresh();

            log.info("Sensitive words cache refreshed after input processing");
        }


        log.info("Sensitive word input completed. totalReceived={}, inserted={}, duplicates={}, invalid={}, changedBy={}",
                totalReceived, savedWords.size(), duplicateWords.size(), invalidWords.size(), changedBy);


        return SensitiveWordInputResponse.builder()
                .totalReceived(totalReceived)
                .inserted(savedWords.size())
                .duplicates(duplicateWords.size())
                .invalid(invalidWords.size())
                .duplicateWords(duplicateWords)
                .invalidWords(invalidWords)
                .build();
    }


    private String normalize(String word) {

        if (word == null) {
            return null;
        }

        return word.trim().toUpperCase(Locale.ROOT);
    }


    private boolean isValid(String word) {

        if (word == null || word.isBlank()) {
            return false;
        }

        return word.length() <= 255;
    }

}