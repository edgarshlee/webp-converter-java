package io.github.edgarshlee.webpconverter.application;

import io.github.edgarshlee.webpconverter.application.port.WebpDecoder;
import io.github.edgarshlee.webpconverter.domain.*;
import io.github.edgarshlee.webpconverter.domain.exception.ImageConversionException;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ImageConversionServiceTest {
    @Test void preservesTransparencyForPngAndResolvesDuplicateNames() throws Exception {
        var service = new ImageConversionService(validDecoder());
        var result = service.convert(List.of(new SourceImage("wallpaper.webp",new byte[]{1}),new SourceImage("wallpaper.webp",new byte[]{2})),new ConversionOptions(OutputFormat.PNG,90,Color.WHITE));
        assertThat(result.failures()).isEmpty();
        assertThat(result.successes()).extracting(ConversionResult::outputName).containsExactly("wallpaper.png","wallpaper_1.png");
        BufferedImage image=ImageIO.read(new ByteArrayInputStream(result.successes().getFirst().content()));
        assertThat(image.getColorModel().hasAlpha()).isTrue();
        assertThat(new Color(image.getRGB(0,0),true).getAlpha()).isZero();
    }
    @Test void flattensTransparencyUsingSelectedJpegBackground() throws Exception {
        var service=new ImageConversionService(validDecoder());
        var result=service.convert(List.of(new SourceImage("wallpaper.webp",new byte[]{1})),new ConversionOptions(OutputFormat.JPG,100,new Color(24,128,210)));
        Color pixel=new Color(ImageIO.read(new ByteArrayInputStream(result.successes().getFirst().content())).getRGB(0,0));
        assertThat(pixel.getRed()).isCloseTo(24,org.assertj.core.data.Offset.offset(5));
        assertThat(pixel.getGreen()).isCloseTo(128,org.assertj.core.data.Offset.offset(5));
        assertThat(pixel.getBlue()).isCloseTo(210,org.assertj.core.data.Offset.offset(5));
    }
    @Test void isolatesPerFileFailure() {
        WebpDecoder decoder=bytes->{if(bytes[0]==0)throw new ImageConversionException("손상된 파일입니다.");return validDecoder().decode(bytes);};
        var result=new ImageConversionService(decoder).convert(List.of(new SourceImage("broken.webp",new byte[]{0}),new SourceImage("valid.webp",new byte[]{1})),new ConversionOptions(OutputFormat.PNG,90,Color.WHITE));
        assertThat(result.successes()).hasSize(1);
        assertThat(result.failures()).containsExactly(new ConversionFailure("broken.webp","손상된 파일입니다."));
    }
    private WebpDecoder validDecoder(){return bytes->{BufferedImage image=new BufferedImage(4,3,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,new Color(255,0,0,0).getRGB());return new DecodedImage(image,false);};}
}
