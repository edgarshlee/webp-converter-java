package io.github.edgarshlee.webpconverter.domain;

import io.github.edgarshlee.webpconverter.domain.exception.InvalidConversionRequestException;
import java.util.Locale;

public enum OutputFormat {
    PNG("png"), JPG("jpg");
    private final String extension;
    OutputFormat(String extension) { this.extension = extension; }
    public String extension() { return extension; }

    public static OutputFormat from(String value) {
        if (value == null) throw new InvalidConversionRequestException("출력 형식을 선택해 주세요.");
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT).replace("JPEG", "JPG"));
        } catch (IllegalArgumentException exception) {
            throw new InvalidConversionRequestException("출력 형식은 PNG 또는 JPG여야 합니다.");
        }
    }
}
