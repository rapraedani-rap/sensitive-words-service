package za.co.flash.sensitivewords.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.flash.sensitivewords.dto.SensitiveWordInputRequest;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SensitiveWordInputStrategyResolver {

    private final List<SensitiveWordInputStrategy> strategies;

    public SensitiveWordInputStrategy resolve(SensitiveWordInputRequest request) {

        return strategies.stream()
                .filter(strategy -> strategy.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported sensitive word input type"));
    }
}