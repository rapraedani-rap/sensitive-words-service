package za.co.flash.sensitivewords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.flash.sensitivewords.entity.SensitiveWordUsage;

public interface SensitiveWordUsageRepository extends JpaRepository<SensitiveWordUsage, Long> {

    @Query(value ="UPDATE SensitiveWordUsage u SET u.usageCount = u.usageCount + 1 " +
            "u.lastUsedAt = CURRENT_TIMESTAMP WHERE u.sensitiveWordId = :sensitiveWordId", nativeQuery = true)
    int incrementUsage(@Param("sensitiveWordId") Long sensitiveWordId);
}