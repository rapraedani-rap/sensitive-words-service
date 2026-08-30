package za.co.flash.sensitivewords.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.flash.sensitivewords.repository.SensitiveWordUsageRepository;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensitiveWordUsageServiceImplTest {

    @Mock
    private SensitiveWordUsageRepository usageRepository;

    private SensitiveWordUsageServiceImpl sensitiveWordUsageService;


    @BeforeEach
    void setUp() {

        sensitiveWordUsageService =
                new SensitiveWordUsageServiceImpl(usageRepository);
    }


    @Test
    void shouldIncrementSensitiveWordUsage() {

        Long sensitiveWordId = 1L;

        sensitiveWordUsageService.increment(sensitiveWordId);

        verify(usageRepository)
                .incrementUsage(sensitiveWordId);
    }
}