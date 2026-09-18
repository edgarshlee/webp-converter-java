package io.github.edgarshlee.webpconverter.infrastructure.imageio;

import io.github.edgarshlee.webpconverter.domain.exception.ImageConversionException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LibWebpImageDecoderTest {
    private final LibWebpImageDecoder decoder = new LibWebpImageDecoder();

    @Test
    void decodesARealWebpImageUsingBundledLibwebp() throws Exception {
        BufferedImage source = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        var graphics = source.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(source, "webp", output)).isTrue();

        var decoded = decoder.decode(output.toByteArray());

        assertThat(decoded.image().getWidth()).isEqualTo(32);
        assertThat(decoded.image().getHeight()).isEqualTo(32);
        Color pixel = new Color(decoded.image().getRGB(16, 16));
        assertThat(pixel.getRed()).isGreaterThan(pixel.getGreen());
        assertThat(pixel.getRed()).isGreaterThan(pixel.getBlue());
    }

    @Test
    void rejectsNonWebpContentBeforeDecoding() {
        assertThatThrownBy(() -> decoder.decode("not-an-image".getBytes()))
                .isInstanceOf(ImageConversionException.class)
                .hasMessage("WebP 이미지가 아닙니다.");
    }
}
