package io.github.edgarshlee.webpconverter.domain;
import java.util.List;
public record BatchConversionResult(List<ConversionResult> successes, List<ConversionFailure> failures) {
    public BatchConversionResult { successes = List.copyOf(successes); failures = List.copyOf(failures); }
}
