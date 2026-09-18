package io.github.edgarshlee.webpconverter.web;
import io.github.edgarshlee.webpconverter.application.ImageConversionService;
import io.github.edgarshlee.webpconverter.application.port.WebpDecoder;
import io.github.edgarshlee.webpconverter.domain.DecodedImage;
import io.github.edgarshlee.webpconverter.domain.exception.ImageConversionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.awt.image.BufferedImage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(controllers=ConversionController.class)
@Import(ImageConversionService.class)
class ConversionControllerTest {
    @Autowired MockMvc mockMvc; @MockitoBean WebpDecoder decoder;
    @Test void returnsZipWithConvertedFileAndReport() throws Exception{when(decoder.decode(any())).thenReturn(new DecodedImage(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB),false));mockMvc.perform(multipart("/api/conversions").file("files","sample".getBytes()).param("format","PNG").param("quality","90").param("background","#ffffff")).andExpect(status().isOk()).andExpect(content().contentType("application/zip")).andExpect(header().string("Content-Disposition",org.hamcrest.Matchers.containsString("attachment")));}
    @Test void returnsProblemDetailWhenEveryFileFails() throws Exception{when(decoder.decode(any())).thenThrow(new ImageConversionException("WebP 이미지가 아닙니다."));mockMvc.perform(multipart("/api/conversions").file("files","not-webp".getBytes())).andExpect(status().isBadRequest()).andExpect(content().contentType("application/problem+json")).andExpect(jsonPath("$.detail").value("WebP 이미지가 아닙니다."));}
}
