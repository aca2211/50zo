module com.example.a50zo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.a50zo to javafx.fxml;
    exports com.example.a50zo;
}