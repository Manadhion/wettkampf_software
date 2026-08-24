package io.github.manadhion.wettkampf.view;

import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Stellt das in der Anwendung eingebettete Wappen des Schützengaus bereit. */
final class GauLogo {

    static final String RESSOURCE = "/io/github/manadhion/wettkampf/gaulogo.png";

    private GauLogo() {
    }

    /**
     * Erzeugt eine geglättete Logoansicht mit fester Höhe und korrektem Seitenverhältnis.
     * @param hoehe gewünschte Höhe in Pixeln
     * @return neue Logoansicht
     */
    static ImageView ansicht(double hoehe) {
        URL url = GauLogo.class.getResource(RESSOURCE);
        if (url == null) {
            throw new IllegalStateException("Gaulogo-Ressource fehlt: " + RESSOURCE);
        }
        ImageView ansicht = new ImageView(new Image(url.toExternalForm()));
        ansicht.setFitHeight(hoehe);
        ansicht.setPreserveRatio(true);
        ansicht.setSmooth(true);
        ansicht.setAccessibleText("Wappen des Schützengaus Rhön-Saale");
        return ansicht;
    }
}
