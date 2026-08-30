package za.co.flash.sensitivewords.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.flash.sensitivewords.entity.SensitiveWordAudit;

public interface SensitiveWordAuditRepository extends JpaRepository<SensitiveWordAudit, Long> {

    Page<SensitiveWordAudit> findAllBySensitiveWord_Id(Long sensitiveWordId,
            Pageable pageable);
}