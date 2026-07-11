module io.github.manadhion {
    requires javafx.controls;
    requires java.sql;
    requires java.prefs;
    requires org.xerial.sqlitejdbc;
    requires javafx.graphics;
    requires java.desktop;
    requires com.github.librepdf.openpdf;

    opens io.github.manadhion.wettkampf.view to javafx.graphics;
}
