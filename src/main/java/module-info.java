module org.example.workloadmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.eclipse.jetty.client;

    opens org.example.workloadmanager to javafx.fxml;
    exports org.example.workloadmanager;
}