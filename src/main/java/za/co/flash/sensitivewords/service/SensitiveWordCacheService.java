package za.co.flash.sensitivewords.service;

import za.co.flash.sensitivewords.model.SensitiveWordRule;

import java.util.List;

public interface SensitiveWordCacheService {

    List<SensitiveWordRule> getRules();

    void refresh();
}