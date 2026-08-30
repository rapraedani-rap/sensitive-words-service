package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import za.co.flash.sensitivewords.service.strategy.SensitiveWordInputStrategy;
import za.co.flash.sensitivewords.service.strategy.SensitiveWordInputStrategyResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordInputServiceImplTest {

    @Mock
    private SensitiveWordInputStrategyResolver strategyResolver;

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @Mock
    private SensitiveWordAuditRepository auditRepository;

    @Mock
    private SensitiveWordUsageRepository usageRepository;

    @Mock
    private SensitiveWordCacheService sensitiveWordCacheService;

    @Mock
    private SensitiveWordInputStrategy strategy;

    private SensitiveWordInputServiceImpl sensitiveWordInputService;

    private String changedBy;


    @BeforeEach
    void setUp() {

        sensitiveWordInputService = new SensitiveWordInputServiceImpl(
                strategyResolver,
                sensitiveWordRepository,
                auditRepository,
                usageRepository,
                sensitiveWordCacheService
        );

        changedBy = "admin";
    }


    @Test
    void shouldProcessAndSaveValidSensitiveWords() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of("select", "drop"));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(2, response.getTotalReceived());
        assertEquals(2, response.getInserted());
        assertEquals(0, response.getDuplicates());
        assertEquals(0, response.getInvalid());

        verify(sensitiveWordRepository).saveAll(any());
        verify(usageRepository).saveAll(any());
        verify(auditRepository).saveAll(any());
        verify(sensitiveWordRepository).flush();
        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldNormalizeSensitiveWordsBeforeSaving() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of("  select  "));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sensitiveWordInputService.process(request, changedBy);

        verify(sensitiveWordRepository).saveAll(argThat(words -> {

            List<SensitiveWord> savedWords = new ArrayList<>((Collection) words);

            return savedWords.size() == 1 &&
                    "SELECT".equals(savedWords.get(0).getWord());
        }));

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldRemoveDuplicatesFromRequest() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of(
                        "SELECT",
                        "select",
                        " SELECT "
                ));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(3, response.getTotalReceived());
        assertEquals(1, response.getInserted());
        assertEquals(2, response.getDuplicates());

        assertTrue(
                response.getDuplicateWords()
                        .contains("SELECT")
        );

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldNotInsertExistingDatabaseWords() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of(
                        "SELECT",
                        "DROP"
                ));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of("SELECT"));

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(2, response.getTotalReceived());
        assertEquals(1, response.getInserted());
        assertEquals(1, response.getDuplicates());

        assertTrue(
                response.getDuplicateWords()
                        .contains("SELECT")
        );

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldMarkBlankWordsAsInvalid() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of(
                        "",
                        "   ",
                        "SELECT"
                ));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(3, response.getTotalReceived());
        assertEquals(1, response.getInserted());
        assertEquals(2, response.getInvalid());

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldMarkWordLongerThan255CharactersAsInvalid() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        String invalidWord = "A".repeat(256);

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of(invalidWord));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(1, response.getTotalReceived());
        assertEquals(0, response.getInserted());
        assertEquals(1, response.getInvalid());

        verify(sensitiveWordRepository).saveAll(any());
        verify(sensitiveWordCacheService, never()).refresh();
    }


    @Test
    void shouldThrowExceptionWhenNoWordsAreSupplied() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> sensitiveWordInputService.process(request, changedBy)
                );

        assertEquals(
                "No sensitive words were supplied",
                exception.getMessage()
        );

        verify(sensitiveWordRepository, never()).saveAll(any());
        verify(usageRepository, never()).saveAll(any());
        verify(auditRepository, never()).saveAll(any());
        verify(sensitiveWordCacheService, never()).refresh();
    }


    @Test
    void shouldCreateUsageRecordForSavedWord() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of("SELECT"));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sensitiveWordInputService.process(request, changedBy);

        verify(usageRepository).saveAll(argThat(records -> {

            SensitiveWordUsage usage =
                    ((List<SensitiveWordUsage>) records).get(0);

            return usage.getUsageCount() == 0L &&
                    "SELECT".equals(
                            usage.getSensitiveWord().getWord()
                    );
        }));

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldCreateAuditRecordWithAuthenticatedUsername() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of("SELECT"));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of());

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        sensitiveWordInputService.process(request, changedBy);

        verify(auditRepository).saveAll(argThat(records -> {

            SensitiveWordAudit audit =
                    ((List<SensitiveWordAudit>) records).get(0);

            return audit.getAction() == AuditAction.CREATE &&
                    "SELECT".equals(audit.getNewValue()) &&
                    "admin".equals(audit.getChangedBy());
        }));

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldNotRefreshCacheWhenNoNewWordsAreInserted() {

        SensitiveWordInputRequest request = new SensitiveWordInputRequest();

        when(strategyResolver.resolve(request))
                .thenReturn(strategy);

        when(strategy.extractWords(request))
                .thenReturn(List.of("SELECT"));

        when(sensitiveWordRepository.findExistingWords(any()))
                .thenReturn(List.of("SELECT"));

        when(sensitiveWordRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SensitiveWordInputResponse response =
                sensitiveWordInputService.process(request, changedBy);

        assertEquals(1, response.getTotalReceived());
        assertEquals(0, response.getInserted());
        assertEquals(1, response.getDuplicates());

        verify(sensitiveWordCacheService, never()).refresh();
    }
}