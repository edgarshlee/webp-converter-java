package io.github.edgarshlee.webpconverter.application.port;
import io.github.edgarshlee.webpconverter.domain.DecodedImage;
public interface WebpDecoder { DecodedImage decode(byte[] content); }
