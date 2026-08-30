package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.flash.sensitivewords.dto.SanitizationResponse;
import za.co.flash.sensitivewords.model.SensitiveWordRule;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SanitizationServiceImplTest {

    @Mock
    private SensitiveWordCacheService sensitiveWordCacheService;

    private SanitizationServiceImpl sanitizationService;


    @BeforeEach
    void setUp() {

        sanitizationService = new SanitizationServiceImpl(
                sensitiveWordCacheService
        );
    }


    @Test
    void shouldSanitizeSensitivePhraseCaseInsensitive() {

        SensitiveWordRule rule = createRule("SELECT * FROM");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(rule));

        SanitizationResponse response =
                sanitizationService.sanitize("select * from the database");

        assertEquals(
                "************* the database",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldNotSanitizePartialWord() {

        SensitiveWordRule rule = createRule("DATA");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(rule));

        SanitizationResponse response =
                sanitizationService.sanitize("database");

        assertEquals(
                "database",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldSanitizeStandaloneWord() {

        SensitiveWordRule rule = createRule("DATA");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(rule));

        SanitizationResponse response =
                sanitizationService.sanitize("DATA is sensitive");

        assertEquals(
                "**** is sensitive",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldNotSanitizePhraseWithDifferentSpacing() {

        SensitiveWordRule rule = createRule("SELECT * FROM");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(rule));

        SanitizationResponse response =
                sanitizationService.sanitize("SELECT    * FROM database");

        assertEquals(
                "SELECT    * FROM database",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldNotSanitizePhraseInDifferentOrder() {

        SensitiveWordRule rule = createRule("SELECT * FROM");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(rule));

        SanitizationResponse response =
                sanitizationService.sanitize("FROM * SELECT database");

        assertEquals(
                "FROM * SELECT database",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldReturnOriginalTextWhenNoSensitiveWordsExist() {

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of());

        SanitizationResponse response =
                sanitizationService.sanitize("normal text");

        assertEquals(
                "normal text",
                response.getSanitizedText()
        );
    }


    @Test
    void shouldSanitizeMultipleSensitiveWords() {

        SensitiveWordRule selectRule = createRule("SELECT");
        SensitiveWordRule dropRule = createRule("DROP");

        when(sensitiveWordCacheService.getRules())
                .thenReturn(List.of(selectRule, dropRule));

        SanitizationResponse response =
                sanitizationService.sanitize("SELECT and DROP");

        assertEquals(
                "****** and ****",
                response.getSanitizedText()
        );
    }


    private SensitiveWordRule createRule(String phrase) {

        return new SensitiveWordRule(
                phrase,
                Pattern.compile(
                        "(?<![\\p{L}\\p{N}_])"
                                + Pattern.quote(phrase)
                                + "(?![\\p{L}\\p{N}_])",
                        Pattern.CASE_INSENSITIVE
                )
        );
    }
}