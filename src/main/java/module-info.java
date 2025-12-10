module com.traffic.sim {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.traffic.sim.controller to javafx.fxml;

    exports com.traffic.sim.app;
}
