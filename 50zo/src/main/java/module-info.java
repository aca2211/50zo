module com.example.a50zo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;



    opens com.example.a50zo to javafx.fxml;
    opens com.example.a50zo.controller to javafx.fxml;
    opens com.example.a50zo.view to javafx.fxml;

    exports com.example.a50zo;
}