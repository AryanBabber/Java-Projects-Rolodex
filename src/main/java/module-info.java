module com.example.fileencryptor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens com.example.fileencryptor to javafx.fxml;
    exports com.example.fileencryptor;
}