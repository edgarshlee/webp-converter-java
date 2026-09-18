package io.github.edgarshlee.webpconverter.domain;

import io.github.edgarshlee.webpconverter.domain.exception.InvalidConversionRequestException;
import java.awt.Color;

public record ConversionOptions(OutputFormat format, int quality, Color background) {
    public ConversionOptions {
        if (format == null) throw new InvalidConversionRequestException("출력 형식을 선택해 주세요.");
        if (quality < 1 || quality > 100) throw new InvalidConversionRequestException("JPG 품질은 1~100이어야 합니다.");
        if (background == null) throw new InvalidConversionRequestException("JPG 배경색을 지정해 주세요.");
    }
    public static ConversionOptions of(String format, int quality, String background) {
        return new ConversionOptions(OutputFormat.from(format), quality, parseColor(background));
    }
    private static Color parseColor(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) throw new InvalidConversionRequestException("배경색은 #RRGGBB 형식이어야 합니다.");
        return new Color(Integer.parseInt(value.substring(1), 16));
    }
    public String backgroundHex() { return "#%02x%02x%02x".formatted(background.getRed(), background.getGreen(), background.getBlue()); }
}
