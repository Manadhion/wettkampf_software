module io.github.manadhion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens io.github.manadhion.wettkampf.view to javafx.fxml;
    exports io.github.manadhion.wettkampf.view;
}
