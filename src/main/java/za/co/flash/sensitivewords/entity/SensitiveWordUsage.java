package za.co.flash.sensitivewords.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensitive_word_usage")
@Getter
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SensitiveWordUsage {

    @Id
    private Long sensitiveWordId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "sensitive_word_id")
    private SensitiveWord sensitiveWord;

    private Long usageCount;

    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if (usageCount == null) {
            usageCount = 0L;
        }
    }
}