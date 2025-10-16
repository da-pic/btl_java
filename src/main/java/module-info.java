module com.example.ql_shopcoffee {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens com.example.ql_shopcoffee to javafx.fxml;
    opens com.example.ql_shopcoffee.controllers to javafx.fxml;
    exports com.example.ql_shopcoffee;
}