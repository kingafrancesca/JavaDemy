package com.example.javademy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.javademy.ui.LoginView;

import java.util.Objects;

/**
 * Punkt wejściowy aplikacji JavaDemy.
 * Inicjalizuje okno główne, ładuje czcionki i przechodzi do widoku logowania.
 */
public class Main extends Application
{
    /** Tworzy instancję aplikacji JavaDemy. */
    public Main() {}

    /**
     * Uruchamia aplikację JavaDemy.
     * Ładuje czcionki i wyświetla widok logowania.
     *
     * @param stage główne okno
     */
    @Override
    public void start(Stage stage)
    {
        loadCustomFonts();

        stage.setTitle("JavaDemy");
        stage.setOnCloseRequest(_ -> { Platform.exit(); System.exit(0); });

        var iconUrl = getClass().getResourceAsStream("/images/logo.png");
        if (iconUrl != null) stage.getIcons().add(new javafx.scene.image.Image(iconUrl));

        showLoginView(stage);
        stage.show();
    }

    private void showLoginView(Stage stage)
    {
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView.getView());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm()
        );

        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.setMaximized(true);
    }

    private void loadCustomFonts()
    {
        String[] fonts = {
                "/fonts/JetBrainsMono-Regular.ttf",
                "/fonts/JetBrainsMono-Bold.ttf",
                "/fonts/PlayfairDisplay-Regular.ttf",
                "/fonts/PlayfairDisplay-Bold.ttf"
        };
        for (String path : fonts) {
            try (var stream = getClass().getResourceAsStream(path)) {
                if (stream != null) {
                    javafx.scene.text.Font.loadFont(stream, 14);
                    System.out.println("Załadowano font: " + path);
                }
            }
            catch (Exception e) {
                System.out.println("Błąd ładowania " + path);
            }
        }
    }

    /** Główna metoda uruchamiająca aplikację.*/
    public static void main(String[] args) {
        launch(args);
    }
}
