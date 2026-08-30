package za.co.flash.sensitivewords.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.flash.sensitivewords.entity.SensitiveWord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    Optional<SensitiveWord> findByWordIgnoreCase(String word);

    boolean existsByWordIgnoreCase(String word);

    Page<SensitiveWord> findAllByActive(Boolean active, Pageable pageable);

    @Query("SELECT s.word FROM SensitiveWord s WHERE s.word IN :words")
    List<String> findExistingWords(@Param("words") Collection<String> words);

    @Query("SELECT s.word FROM SensitiveWord s WHERE s.active = true")
    List<String> findAllActiveWords();
}