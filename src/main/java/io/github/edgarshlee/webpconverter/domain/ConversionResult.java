package io.github.edgarshlee.webpconverter.domain;

public record ConversionResult(String sourceName, String outputName, byte[] content, boolean animated) {
    public ConversionResult { content = content.clone(); }
    @Override public byte[] content() { return content.clone(); }
}
