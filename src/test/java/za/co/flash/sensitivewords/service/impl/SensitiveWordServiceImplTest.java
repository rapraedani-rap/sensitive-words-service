package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.entity.SensitiveWordUsage;
import za.co.flash.sensitivewords.exception.SensitiveWordException;
import za.co.flash.sensitivewords.repository.SensitiveWordRepository;
import za.co.flash.sensitivewords.repository.SensitiveWordUsageRepository;
import za.co.flash.sensitivewords.service.SensitiveWordAuditService;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceImplTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @Mock
    private SensitiveWordUsageRepository usageRepository;

    @Mock
    private SensitiveWordAuditService auditService;

    @Mock
    private SensitiveWordCacheService sensitiveWordCacheService;

    private SensitiveWordServiceImpl sensitiveWordService;

    private String changedBy;


    @BeforeEach
    void setUp() {

        sensitiveWordService = new SensitiveWordServiceImpl(sensitiveWordRepository,
                usageRepository, auditService, sensitiveWordCacheService);

        changedBy = "admin";
    }


    @Test
    void shouldCreateSensitiveWord() {

        SensitiveWord savedWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordRepository.existsByWordIgnoreCase("SELECT"))
                .thenReturn(false);

        when(sensitiveWordRepository.save(any(SensitiveWord.class)))
                .thenReturn(savedWord);

        SensitiveWord result =
                sensitiveWordService.create(" select ", changedBy);

        assertEquals(1L, result.getId());
        assertEquals("SELECT", result.getWord());
        assertTrue(result.getActive());

        verify(sensitiveWordRepository)
                .existsByWordIgnoreCase("SELECT");

        verify(sensitiveWordRepository)
                .save(any(SensitiveWord.class));

        verify(usageRepository)
                .save(any(SensitiveWordUsage.class));

        verify(auditService)
                .recordCreate(savedWord, "admin");

        verify(sensitiveWordCacheService)
                .refresh();
    }


    @Test
    void shouldThrowExceptionWhenSensitiveWordAlreadyExists() {

        when(sensitiveWordRepository.existsByWordIgnoreCase("SELECT"))
                .thenReturn(true);

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.create("select", changedBy)
                );

        assertEquals(
                "Sensitive word already exists: SELECT",
                exception.getMessage()
        );

        verify(sensitiveWordRepository, never())
                .save(any());

        verify(usageRepository, never())
                .save(any());

        verify(auditService, never())
                .recordCreate(any(), anyString());

        verify(sensitiveWordCacheService, never())
                .refresh();
    }


    @Test
    void shouldThrowExceptionWhenWordIsNull() {

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.create(null, changedBy)
                );

        assertEquals(
                "Sensitive word is required",
                exception.getMessage()
        );

        verifyNoInteractions(sensitiveWordRepository);

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldThrowExceptionWhenWordIsBlank() {

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.create("   ", changedBy)
                );

        assertEquals(
                "Sensitive word is required",
                exception.getMessage()
        );

        verifyNoInteractions(sensitiveWordRepository);

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldThrowExceptionWhenWordExceeds255Characters() {

        String word = "A".repeat(256);

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.create(word, changedBy)
                );

        assertEquals(
                "Sensitive word cannot exceed 255 characters",
                exception.getMessage()
        );

        verifyNoInteractions(sensitiveWordRepository);

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldFindSensitiveWordByWord() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("select"))
                .thenReturn(Optional.of(sensitiveWord));

        SensitiveWord result =
                sensitiveWordService.findByWord("select");

        assertEquals("SELECT", result.getWord());
        assertEquals(1L, result.getId());

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("select");

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldThrowExceptionWhenSensitiveWordIsNotFound() {

        when(sensitiveWordRepository.findByWordIgnoreCase("UNKNOWN"))
                .thenReturn(Optional.empty());

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.findByWord("UNKNOWN")
                );

        assertEquals(
                "Sensitive word not found: UNKNOWN",
                exception.getMessage()
        );

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldFindAllSensitiveWordsByActiveStatus() {

        PageRequest pageable = PageRequest.of(0, 10);

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        Page<SensitiveWord> page =
                new PageImpl<>(List.of(sensitiveWord));

        when(sensitiveWordRepository.findAllByActive(true, pageable))
                .thenReturn(page);

        Page<SensitiveWord> result =
                sensitiveWordService.findAll(true, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("SELECT", result.getContent().get(0).getWord());

        verify(sensitiveWordRepository)
                .findAllByActive(true, pageable);

        verifyNoInteractions(sensitiveWordCacheService);
    }


    @Test
    void shouldDisableSensitiveWord() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        when(sensitiveWordRepository.save(sensitiveWord))
                .thenReturn(sensitiveWord);

        SensitiveWord result =
                sensitiveWordService.disable("SELECT", changedBy);

        assertFalse(result.getActive());

        verify(sensitiveWordRepository)
                .save(sensitiveWord);

        verify(auditService)
                .recordDisable(sensitiveWord, "admin");

        verify(sensitiveWordCacheService)
                .refresh();
    }


    @Test
    void shouldReturnWordWithoutSavingWhenAlreadyDisabled() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(false)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        SensitiveWord result =
                sensitiveWordService.disable("SELECT", changedBy);

        assertFalse(result.getActive());

        verify(sensitiveWordRepository, never())
                .save(any());

        verify(auditService, never())
                .recordDisable(any(), anyString());

        verify(sensitiveWordCacheService, never())
                .refresh();
    }


    @Test
    void shouldEnableSensitiveWord() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(false)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        when(sensitiveWordRepository.save(sensitiveWord))
                .thenReturn(sensitiveWord);

        SensitiveWord result =
                sensitiveWordService.enable("SELECT", changedBy);

        assertTrue(result.getActive());

        verify(sensitiveWordRepository)
                .save(sensitiveWord);

        verify(auditService)
                .recordEnable(sensitiveWord, "admin");

        verify(sensitiveWordCacheService)
                .refresh();
    }


    @Test
    void shouldReturnWordWithoutSavingWhenAlreadyEnabled() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        SensitiveWord result =
                sensitiveWordService.enable("SELECT", changedBy);

        assertTrue(result.getActive());

        verify(sensitiveWordRepository, never())
                .save(any());

        verify(auditService, never())
                .recordEnable(any(), anyString());

        verify(sensitiveWordCacheService, never())
                .refresh();
    }


    @Test
    void shouldUpdateSensitiveWord() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        when(sensitiveWordRepository.findByWordIgnoreCase("UPDATE"))
                .thenReturn(Optional.empty());

        when(sensitiveWordRepository.save(sensitiveWord))
                .thenReturn(sensitiveWord);

        SensitiveWord result =
                sensitiveWordService.update("SELECT", "UPDATE", changedBy);

        assertEquals("UPDATE", result.getWord());

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("SELECT");

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("UPDATE");

        verify(sensitiveWordRepository)
                .save(sensitiveWord);

        verify(auditService).recordUpdate(sensitiveWord, "SELECT",
                "UPDATE",
                "admin"
        );

        verify(sensitiveWordCacheService).refresh();
    }


    @Test
    void shouldThrowExceptionWhenUpdatingSensitiveWordIsNotFound() {

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.empty());

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.update("SELECT", "UPDATE", changedBy)
                );

        assertEquals(
                "Sensitive word not found: SELECT",
                exception.getMessage()
        );

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("SELECT");

        verify(sensitiveWordRepository, never())
                .save(any());

        verify(auditService, never())
                .recordUpdate(any(), anyString(), anyString(), anyString());

        verify(sensitiveWordCacheService, never())
                .refresh();
    }


    @Test
    void shouldThrowExceptionWhenUpdatedSensitiveWordAlreadyExists() {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        SensitiveWord existingWord = SensitiveWord.builder()
                .id(2L)
                .word("UPDATE")
                .active(true)
                .build();

        when(sensitiveWordRepository.findByWordIgnoreCase("SELECT"))
                .thenReturn(Optional.of(sensitiveWord));

        when(sensitiveWordRepository.findByWordIgnoreCase("UPDATE"))
                .thenReturn(Optional.of(existingWord));

        SensitiveWordException exception =
                assertThrows(
                        SensitiveWordException.class,
                        () -> sensitiveWordService.update("SELECT", "UPDATE", changedBy)
                );

        assertEquals(
                "Sensitive word already exists: UPDATE",
                exception.getMessage()
        );

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("SELECT");

        verify(sensitiveWordRepository)
                .findByWordIgnoreCase("UPDATE");

        verify(sensitiveWordRepository, never())
                .save(any());

        verify(auditService, never())
                .recordUpdate(any(), anyString(), anyString(), anyString());

        verify(sensitiveWordCacheService, never())
                .refresh();
    }
}