module io.github.manadhion {
    requires javafx.controls;
    requires java.sql;
    requires java.prefs;
    requires org.xerial.sqlitejdbc;
    requires javafx.graphics;
    requires java.desktop;
    requires java.net.http;
    requires com.github.librepdf.openpdf;
    requires com.sun.jna.platform;

    opens io.github.manadhion.wettkampf.view to javafx.graphics;
}
