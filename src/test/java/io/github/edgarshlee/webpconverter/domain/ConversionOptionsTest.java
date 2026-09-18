package io.github.edgarshlee.webpconverter.domain;
import io.github.edgarshlee.webpconverter.domain.exception.InvalidConversionRequestException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ConversionOptionsTest {
    @Test void acceptsJpegAliasAndParsesBackground(){var options=ConversionOptions.of("jpeg",85,"#12Abef");assertThat(options.format()).isEqualTo(OutputFormat.JPG);assertThat(options.backgroundHex()).isEqualTo("#12abef");}
    @Test void rejectsInvalidQualityAndColor(){assertThatThrownBy(()->ConversionOptions.of("PNG",0,"#ffffff")).isInstanceOf(InvalidConversionRequestException.class);assertThatThrownBy(()->ConversionOptions.of("PNG",90,"white")).isInstanceOf(InvalidConversionRequestException.class);}
}
