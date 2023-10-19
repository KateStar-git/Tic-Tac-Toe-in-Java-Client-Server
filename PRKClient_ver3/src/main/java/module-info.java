module com.example.tictactoeclient2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tictactoeclient3 to javafx.fxml;
    exports com.example.tictactoeclient3;
}