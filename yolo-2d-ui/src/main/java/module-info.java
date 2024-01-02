module nicok.thesis.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;


    opens nicok.thesis.ui to javafx.fxml;
    exports nicok.thesis.ui;
}