package io.github.edgarshlee.webpconverter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edgarshlee.webpconverter.application.ImageConversionService;
import io.github.edgarshlee.webpconverter.domain.*;
import io.github.edgarshlee.webpconverter.domain.exception.InvalidConversionRequestException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.*;

@RestController
public class ConversionController {
    private static final int MAX_FILES = 20;
    private final ImageConversionService conversionService;
    private final ObjectMapper objectMapper;

    public ConversionController(ImageConversionService conversionService, ObjectMapper objectMapper) {
        this.conversionService = conversionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/api/conversions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "PNG") String format,
            @RequestParam(defaultValue = "90") int quality,
            @RequestParam(defaultValue = "#ffffff") String background) {
        validateFiles(files);
        ConversionOptions options = ConversionOptions.of(format, quality, background);
        BatchConversionResult result = conversionService.convert(readFiles(files), options);
        if (result.successes().isEmpty()) {
            String reason = result.failures().isEmpty() ? "변환 가능한 파일이 없습니다." : result.failures().getFirst().reason();
            throw new InvalidConversionRequestException(reason);
        }
        byte[] zip = createZip(result, options);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("webp-converted-%d.zip".formatted(Instant.now().toEpochMilli())).build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType("application/zip")).contentLength(zip.length).body(zip);
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty))
            throw new InvalidConversionRequestException("WebP 파일을 하나 이상 선택해 주세요.");
        if (files.size() > MAX_FILES)
            throw new InvalidConversionRequestException("한 번에 최대 %d개까지 변환할 수 있습니다.".formatted(MAX_FILES));
    }

    private List<SourceImage> readFiles(List<MultipartFile> files) {
        List<SourceImage> sources = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try { sources.add(new SourceImage(file.getOriginalFilename(), file.getBytes())); }
            catch (IOException exception) { throw new InvalidConversionRequestException("업로드 파일을 읽지 못했습니다."); }
        }
        return sources;
    }

    private byte[] createZip(BatchConversionResult result, ConversionOptions options) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output)) {
            for (ConversionResult converted : result.successes()) {
                zip.putNextEntry(new ZipEntry(converted.outputName()));
                zip.write(converted.content());
                zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry("conversion-report.json"));
            zip.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report(result, options)));
            zip.closeEntry();
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("변환 결과 압축 파일을 만들지 못했습니다.", exception); }
    }

    private Map<String, Object> report(BatchConversionResult result, ConversionOptions options) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("format", options.format().name());
        report.put("quality", options.quality());
        report.put("background", options.backgroundHex());
        report.put("successCount", result.successes().size());
        report.put("failureCount", result.failures().size());
        report.put("successes", result.successes().stream().map(item -> Map.of(
                "source", item.sourceName(), "output", item.outputName(), "animatedSource", item.animated())).toList());
        report.put("failures", result.failures());
        return report;
    }
}
