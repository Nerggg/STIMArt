module com.stimart {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.stimart to javafx.fxml;
    exports com.stimart;
    exports com.stimart.Controller;
    opens com.stimart.Controller to javafx.fxml;
}