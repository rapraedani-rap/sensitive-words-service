package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.entity.SensitiveWordAudit;
import za.co.flash.sensitivewords.enums.AuditAction;
import za.co.flash.sensitivewords.repository.SensitiveWordAuditRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensitiveWordAuditServiceImplTest {

    @Mock
    private SensitiveWordAuditRepository auditRepository;

    private SensitiveWordAuditServiceImpl sensitiveWordAuditService;


    @BeforeEach
    void setUp() {

        sensitiveWordAuditService =
                new SensitiveWordAuditServiceImpl(auditRepository);
    }


    @Test
    void shouldRecordCreateAudit() {

        SensitiveWord word = SensitiveWord.builder()
                .word("SELECT")
                .build();

        sensitiveWordAuditService.recordCreate(word, "admin");

        ArgumentCaptor<SensitiveWordAudit> captor =
                ArgumentCaptor.forClass(SensitiveWordAudit.class);

        verify(auditRepository).save(captor.capture());

        SensitiveWordAudit audit = captor.getValue();

        assertEquals(word, audit.getSensitiveWord());
        assertEquals(AuditAction.CREATE, audit.getAction());
        assertNull(audit.getOldValue());
        assertEquals("SELECT", audit.getNewValue());
        assertEquals("admin", audit.getChangedBy());
    }


    @Test
    void shouldRecordUpdateAudit() {

        SensitiveWord word = SensitiveWord.builder()
                .word("UPDATE")
                .build();

        sensitiveWordAuditService.recordUpdate(
                word,
                "SELECT",
                "UPDATE",
                "admin"
        );

        ArgumentCaptor<SensitiveWordAudit> captor =
                ArgumentCaptor.forClass(SensitiveWordAudit.class);

        verify(auditRepository).save(captor.capture());

        SensitiveWordAudit audit = captor.getValue();

        assertEquals(word, audit.getSensitiveWord());
        assertEquals(AuditAction.UPDATE, audit.getAction());
        assertEquals("SELECT", audit.getOldValue());
        assertEquals("UPDATE", audit.getNewValue());
        assertEquals("admin", audit.getChangedBy());
    }


    @Test
    void shouldRecordDisableAudit() {

        SensitiveWord word = SensitiveWord.builder()
                .word("DROP")
                .build();

        sensitiveWordAuditService.recordDisable(word, "admin");

        ArgumentCaptor<SensitiveWordAudit> captor =
                ArgumentCaptor.forClass(SensitiveWordAudit.class);

        verify(auditRepository).save(captor.capture());

        SensitiveWordAudit audit = captor.getValue();

        assertEquals(word, audit.getSensitiveWord());
        assertEquals(AuditAction.DISABLE, audit.getAction());
        assertEquals("DROP", audit.getOldValue());
        assertEquals("DROP", audit.getNewValue());
        assertEquals("admin", audit.getChangedBy());
    }


    @Test
    void shouldRecordEnableAudit() {

        SensitiveWord word = SensitiveWord.builder()
                .word("DROP")
                .build();

        sensitiveWordAuditService.recordEnable(word, "admin");

        ArgumentCaptor<SensitiveWordAudit> captor =
                ArgumentCaptor.forClass(SensitiveWordAudit.class);

        verify(auditRepository).save(captor.capture());

        SensitiveWordAudit audit = captor.getValue();

        assertEquals(word, audit.getSensitiveWord());
        assertEquals(AuditAction.ENABLE, audit.getAction());
        assertEquals("DROP", audit.getOldValue());
        assertEquals("DROP", audit.getNewValue());
        assertEquals("admin", audit.getChangedBy());
    }
}