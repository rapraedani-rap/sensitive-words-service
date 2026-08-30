package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.flash.sensitivewords.model.SensitiveWordRule;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveWordCacheServiceImplTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    private SensitiveWordCacheServiceImpl sensitiveWordCacheService;


    @BeforeEach
    void setUp() {

        sensitiveWordCacheService =
                new SensitiveWordCacheServiceImpl(
                        sensitiveWordRepository
                );
    }


    @Test
    void shouldLoadActiveWordsIntoCache() {

        when(sensitiveWordRepository.findAllActiveWords()).thenReturn(List.of(
                        "DROP",
                        "SELECT * FROM",
                        "DATA"
                ));

        sensitiveWordCacheService.refresh();

        List<SensitiveWordRule> rules =
                sensitiveWordCacheService.getRules();

        assertEquals(3, rules.size());
    }


    @Test
    void shouldOrderLongestPhraseFirst() {

        when(sensitiveWordRepository.findAllActiveWords())
                .thenReturn(List.of(
                        "SELECT",
                        "SELECT * FROM"
                ));

        sensitiveWordCacheService.refresh();

        List<SensitiveWordRule> rules =
                sensitiveWordCacheService.getRules();

        assertEquals(
                "SELECT * FROM",
                rules.get(0).getWord()
        );

        assertEquals(
                "SELECT",
                rules.get(1).getWord()
        );
    }


    @Test
    void shouldCreateCaseInsensitivePattern() {

        when(sensitiveWordRepository.findAllActiveWords())
                .thenReturn(List.of("DATA"));

        sensitiveWordCacheService.refresh();

        SensitiveWordRule rule =
                sensitiveWordCacheService.getRules().get(0);

        assertTrue(
                rule.getPattern()
                        .matcher("data")
                        .find()
        );
    }


    @Test
    void shouldNotMatchInsideAnotherWord() {

        when(sensitiveWordRepository.findAllActiveWords())
                .thenReturn(List.of("DATA"));

        sensitiveWordCacheService.refresh();

        SensitiveWordRule rule =
                sensitiveWordCacheService.getRules().get(0);

        assertTrue(
                !rule.getPattern()
                        .matcher("database")
                        .find()
        );
    }
}