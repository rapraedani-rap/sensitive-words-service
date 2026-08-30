package za.co.flash.sensitivewords.entity;

import jakarta.persistence.*;
import lombok.*;
import za.co.flash.sensitivewords.enums.AuditAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensitive_word_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensitive_word_id")
    private SensitiveWord sensitiveWord;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    private String oldValue;

    private String newValue;

    private String changedBy;

    private LocalDateTime changedAt;

}