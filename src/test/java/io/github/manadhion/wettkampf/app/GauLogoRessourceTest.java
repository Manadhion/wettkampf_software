package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class GauLogoRessourceTest {

    private static final String GAULOGO = "/io/github/manadhion/wettkampf/gaulogo.png";

    @Test
    void gaulogoIstUnveraendertUndLesbarEingebettet() throws Exception {
        try (InputStream eingabe = GauLogoRessourceTest.class.getResourceAsStream(GAULOGO)) {
            assertNotNull(eingabe, "Das Gaulogo muss im Anwendungs-JAR enthalten sein");
            BufferedImage bild = ImageIO.read(eingabe);
            assertNotNull(bild, "Das Gaulogo muss ein lesbares PNG sein");
            assertEquals(201, bild.getWidth());
            assertEquals(234, bild.getHeight());
        }
    }
}
