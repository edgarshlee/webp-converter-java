package io.github.edgarshlee.webpconverter.domain;

public record SourceImage(String filename, byte[] content) {
    public SourceImage {
        filename = filename == null || filename.isBlank() ? "unnamed.webp" : filename;
        content = content == null ? new byte[0] : content.clone();
    }
    @Override public byte[] content() { return content.clone(); }
}
