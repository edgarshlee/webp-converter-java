package io.github.edgarshlee.webpconverter.application;

import io.github.edgarshlee.webpconverter.application.port.WebpDecoder;
import io.github.edgarshlee.webpconverter.domain.*;
import io.github.edgarshlee.webpconverter.domain.exception.ImageConversionException;
import org.springframework.stereotype.Service;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.Normalizer;
import java.util.List;
import java.util.*;

@Service
public class ImageConversionService {
    private final WebpDecoder decoder;

    public ImageConversionService(WebpDecoder decoder) { this.decoder = decoder; }

    public BatchConversionResult convert(List<SourceImage> sources, ConversionOptions options) {
        List<ConversionResult> successes = new ArrayList<>();
        List<ConversionFailure> failures = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        for (SourceImage source : sources) {
            try {
                DecodedImage decoded = decoder.decode(source.content());
                String outputName = uniqueName(source.filename(), options.format(), usedNames);
                successes.add(new ConversionResult(source.filename(), outputName,
                        encode(decoded.image(), options), decoded.animated()));
            } catch (ImageConversionException exception) {
                failures.add(new ConversionFailure(source.filename(), exception.getMessage()));
            } catch (RuntimeException exception) {
                failures.add(new ConversionFailure(source.filename(), "이미지를 변환하지 못했습니다."));
            }
        }
        return new BatchConversionResult(successes, failures);
    }

    private byte[] encode(BufferedImage source, ConversionOptions options) {
        return switch (options.format()) {
            case PNG -> writePng(copyToArgb(source));
            case JPG -> writeJpeg(flatten(source, options.background()), options.quality());
        };
    }

    private BufferedImage copyToArgb(BufferedImage source) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try { graphics.drawImage(source, 0, 0, null); } finally { graphics.dispose(); }
        return target;
    }

    private BufferedImage flatten(BufferedImage source, Color background) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(background);
            graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally { graphics.dispose(); }
        return target;
    }

    private byte[] writePng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "PNG", output)) throw new ImageConversionException("PNG 인코더를 찾을 수 없습니다.");
            return output.toByteArray();
        } catch (IOException exception) {
            throw new ImageConversionException("PNG 파일을 만들지 못했습니다.", exception);
        }
    }

    private byte[] writeJpeg(BufferedImage image, int quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
        if (!writers.hasNext()) throw new ImageConversionException("JPG 인코더를 찾을 수 없습니다.");
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality / 100f);
            writer.write(null, new IIOImage(image, null, null), params);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new ImageConversionException("JPG 파일을 만들지 못했습니다.", exception);
        } finally { writer.dispose(); }
    }

    private String uniqueName(String originalName, OutputFormat format, Set<String> usedNames) {
        String safe = sanitizeBaseName(originalName);
        String candidate = safe + "." + format.extension();
        int index = 1;
        while (!usedNames.add(candidate.toLowerCase(Locale.ROOT))) candidate = safe + "_" + index++ + "." + format.extension();
        return candidate;
    }

    private String sanitizeBaseName(String filename) {
        String leaf = filename.replace('\\', '/');
        leaf = leaf.substring(leaf.lastIndexOf('/') + 1);
        int dot = leaf.lastIndexOf('.');
        String base = dot > 0 ? leaf.substring(0, dot) : leaf;
        base = Normalizer.normalize(base, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}<>:\"/\\\\|?*]", "_").replaceAll("[. ]+$", "").trim();
        if (base.isBlank()) base = "converted";
        return base.substring(0, Math.min(base.length(), 120));
    }
}
