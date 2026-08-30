package za.co.flash.sensitivewords.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.flash.sensitivewords.model.SensitiveWordRule;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordCacheServiceImpl implements SensitiveWordCacheService {

    private final SensitiveWordRepository sensitiveWordRepository;

    private volatile List<SensitiveWordRule> rules = List.of();

    @PostConstruct
    public void initialiseWords() {

        log.info("Initialising sensitive words cache");

        refresh();

        log.info("Sensitive words cache initialised successfully. rules={}", rules.size());
    }

    @Override
    public List<SensitiveWordRule> getRules() {

        return rules;
    }


    @Override
    public void refresh() {

        log.info("Refreshing sensitive words cache");

        rules = sensitiveWordRepository.findAllActiveWords().stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(phrase -> new SensitiveWordRule(phrase, Pattern.compile("(?<![\\p{L}\\p{N}_])"
                        + Pattern.quote(phrase) + "(?![\\p{L}\\p{N}_])", Pattern.CASE_INSENSITIVE))).toList();

        log.info("Sensitive words cache refreshed successfully. rules={}", rules.size());
    }
}