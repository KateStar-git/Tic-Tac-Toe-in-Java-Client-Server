package com.example.tictactoeclient3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Klasa rozszerzaj¹ca u¿ywaj¹ca interfejsu Application
 * w celu uruchomienia pliku fxml.
 * Okreœla rozmiar okna gry, umo¿liwia jego skalowalnoœæ oraz ukazanie siê graficznej wersji gry
 */
public class TicTacToeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TicTacToeApplication.class.getResource("tic-tac-toe-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);
        stage.setTitle("Tic Tac Toe");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}