package io.github.edgarshlee.webpconverter.infrastructure.imageio;

import io.github.edgarshlee.webpconverter.application.port.WebpDecoder;
import io.github.edgarshlee.webpconverter.domain.DecodedImage;
import io.github.edgarshlee.webpconverter.domain.exception.ImageConversionException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class LibWebpImageDecoder implements WebpDecoder {
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};
    private static final String LIBWEBP_READER_PACKAGE = "com.luciad.imageio.webp";

    @Override
    public DecodedImage decode(byte[] content) {
        validateSignature(content);
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            ImageReader reader = findLibWebpReader();
            try {
                reader.setInput(input, false, true);
                int count = imageCount(reader);
                BufferedImage image = reader.read(0);
                if (image == null) throw new ImageConversionException("WebP 이미지를 읽지 못했습니다.");
                return new DecodedImage(image, count > 1);
            } finally {
                reader.dispose();
            }
        } catch (ImageConversionException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new ImageConversionException("지원하지 않거나 손상된 WebP 파일입니다.", exception);
        }
    }

    private ImageReader findLibWebpReader() {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/webp");
        while (readers.hasNext()) {
            ImageReader reader = readers.next();
            if (reader.getClass().getPackageName().startsWith(LIBWEBP_READER_PACKAGE)) return reader;
            reader.dispose();
        }
        throw new ImageConversionException("libwebp 디코더를 찾을 수 없습니다.");
    }

    private int imageCount(ImageReader reader) {
        try { return reader.getNumImages(true); }
        catch (IOException | UnsupportedOperationException ignored) { return 1; }
    }

    private void validateSignature(byte[] content) {
        if (content.length < 12 || !matches(content, 0, RIFF) || !matches(content, 8, WEBP))
            throw new ImageConversionException("WebP 이미지가 아닙니다.");
    }

    private boolean matches(byte[] content, int offset, byte[] expected) {
        for (int i = 0; i < expected.length; i++) if (content[offset + i] != expected[i]) return false;
        return true;
    }
}
