package za.co.flash.sensitivewords.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public class SensitiveWordRule {

    private String word;

    private Pattern pattern;
}