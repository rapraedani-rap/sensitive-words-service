package za.co.flash.sensitivewords.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.flash.sensitivewords.repository.SensitiveWordUsageRepository;
import za.co.flash.sensitivewords.service.SensitiveWordUsageService;

@Service
@RequiredArgsConstructor
public class SensitiveWordUsageServiceImpl implements SensitiveWordUsageService {

    private final SensitiveWordUsageRepository usageRepository;

    @Override
    @Transactional
    public void increment(Long sensitiveWordId) {
        usageRepository.incrementUsage(sensitiveWordId);
    }
}