module io.github.manadhion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires javafx.graphics;

    opens io.github.manadhion.wettkampf.view to javafx.fxml, javafx.graphics;
}
