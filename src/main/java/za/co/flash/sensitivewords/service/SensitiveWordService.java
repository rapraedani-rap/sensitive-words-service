package za.co.flash.sensitivewords.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import za.co.flash.sensitivewords.entity.SensitiveWord;

public interface SensitiveWordService {

    SensitiveWord create(String word, String changedBy);

    @Transactional(readOnly = true)
    SensitiveWord findByWord(String word);

    @Transactional(readOnly = true)
    Page<SensitiveWord> findAll(Boolean active, Pageable pageable);

    SensitiveWord update(String word, String changedBy);

    SensitiveWord enable(String word, String changedBy);

    SensitiveWord disable(String word, String changedBy);
}