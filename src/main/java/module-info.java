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

    requires com.google.protobuf;
    requires io.grpc;
    requires io.grpc.stub;
    requires io.grpc.protobuf;
    requires com.google.common;
    requires static lombok;
    requires tools.jackson.databind;


    exports org.example.workloadmanager;
    exports org.example.workloadmanager.Network;
    opens org.example.workloadmanager.Network to javafx.fxml;
    exports org.example.workloadmanager.SettingWorkload;
    opens org.example.workloadmanager.SettingWorkload to javafx.fxml;
}
