package com.example.javademy.ui;
import com.example.javademy.auth.CurrentUser;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Główne okno aplikacji JavaDemy po zalogowaniu.
 * Zarządza nawigacją między widokami: Dashboard, Lekcje, Code Lab, Zadania praktyczne i Quizy.
 * Zawiera pasek menu z opcjami: Plik, Edycja i Pomoc.
 */
public class MainWindow
{
    private final Stage stage;
    private StackPane contentArea;

    /**
     * Tworzy MainWindow dla wskazanego okna.
     *
     * @param stage główne okno aplikacji.
     */
    public MainWindow(Stage stage) { this.stage = stage; }

    /**
     * Wyświetla okno główne z paskiem menu i dashboardem.
     */
    public void show()
    {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5ECE1;");
        root.setTop(buildMenuBar());

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F5ECE1;");
        root.setCenter(contentArea);

        showDashboard();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm());
        stage.setScene(scene);
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            Platform.runLater(() -> stage.setMaximized(true));
        }
        else stage.setMaximized(true);
    }

    private MenuBar buildMenuBar()
    {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #F5ECE1; -fx-border-color: transparent transparent #DDD0C0 transparent; -fx-border-width: 0 0 1 0;");

        Menu fileMenu = new Menu("Plik");
        MenuItem exitItem = new MenuItem("Zamknij");
        exitItem.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        exitItem.setOnAction(_ -> { Platform.exit(); System.exit(0); });
        fileMenu.getItems().add(exitItem);

        Menu editMenu = new Menu("Edytuj");
        MenuItem clearProgressItem = new MenuItem("Wyczyść historię prób");
        clearProgressItem.setOnAction(_ -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Potwierdzenie");
            alert.setHeaderText("Wyczyścić historię prób?");
            alert.setContentText("Ta operacja jest nieodwracalna.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK)
                    new com.example.javademy.storage.DataStorage().saveAttempts(
                            CurrentUser.getInstance().getStudent().getLogin(),
                            new java.util.ArrayList<>()
                    );
            });
        });
        editMenu.getItems().add(clearProgressItem);

        Menu helpMenu = new Menu("Pomoc");
        MenuItem helpItem = new MenuItem("Jak korzystać z aplikacji?");
        helpItem.setOnAction(_ -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pomoc");
            alert.setHeaderText("Jak korzystać z JavaDemy?");
            alert.setContentText("Lekcje - przeglądaj materiał teoretyczny\nCodeLab - pisz i uruchamiaj kod Java\nZadania - praktyczne zadania programistyczne\nQuizy - sprawdź swoją wiedzę");
            alert.showAndWait();
        });
        MenuItem aboutItem = new MenuItem("O aplikacji");
        aboutItem.setOnAction(_ -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("O aplikacji");
            alert.setHeaderText("JavaDemy");
            alert.setContentText("Wersja: 1.0\nInteraktywna platforma do nauki języka Java.\n\nAutor: Kinga Kinowska");
            alert.showAndWait();
        });
        helpMenu.getItems().addAll(helpItem, new SeparatorMenuItem(), aboutItem);
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        return menuBar;
    }

    private boolean isPrefixed(String dest) {
        return dest.startsWith("lekcja:") || dest.startsWith("repair-quiz:")
            || dest.startsWith("repair-code:") || dest.startsWith("quiz-lesson:");
    }

    private void showDashboard()
    {
        ProgressDashboard dashboard = new ProgressDashboard(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("dashboard");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(dashboard.getView());
    }

    private void showLessons()
    {
        LessonView lessonView = new LessonView();
        lessonView.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("lekcje");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(lessonView.getView());
        StackPane.setAlignment(lessonView.getView(), javafx.geometry.Pos.TOP_LEFT);
    }

    private void showCodeLab()
    {
        CodeEditorView v = new CodeEditorView();
        v.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("codelab");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(v.getView());
    }

    private void showCodeExercises()
    {
        showCodeExercises(null);
    }

    private void showCodeExercises(String focusExerciseId)
    {
        CodeExerciseView v = new CodeExerciseView(null, focusExerciseId);
        v.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("zadania");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(v.getView());
    }

    private void showQuiz()
    {
        QuizView v = new QuizView();
        v.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "quizy" -> showQuiz();
                case "zadania" -> showCodeExercises();
                case "codelab" -> showCodeLab();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("quizy");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(v.getView());
    }

    private void showRepairPlan()
    {
        RepairPlanView v = new RepairPlanView();
        v.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("zaleglosci");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(v.getView());
    }

    private void handleRepairNav(String dest)
    {
        if (dest.startsWith("repair-code:")) showCodeExercises(dest.substring("repair-code:".length()));
        else if (dest.startsWith("repair-quiz:")) showFlashcardsForLesson(dest.substring("repair-quiz:".length()));
        else if (dest.startsWith("quiz-lesson:")) showQuizForLesson(dest.substring("quiz-lesson:".length()));
        else if (dest.startsWith("lekcja:")) showLessonById(dest.substring("lekcja:".length()));
    }

    private void showLessonById(String lessonId)
    {
        LessonView lv = new LessonView();
        lv.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("lekcje");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(lv.getView());
        StackPane.setAlignment(lv.getView(), javafx.geometry.Pos.TOP_LEFT);
        lv.openLessonById(lessonId);
    }

    private void showFlashcardsForLesson(String lessonId)
    {
        QuizView qv = new QuizView();
        qv.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "quizy" -> showQuiz();
                case "zadania" -> showCodeExercises();
                case "codelab" -> showCodeLab();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("zaleglosci");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(qv.getView());
        new com.example.javademy.storage.DataStorage().loadLessons().stream().filter(l -> lessonId.equals(l.getId())).findFirst().ifPresent(qv::startFlashcards);
    }

    private void showQuizForLesson(String lessonId)
    {
        QuizView qv = new QuizView();
        qv.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "quizy" -> showQuiz();
                case "zadania" -> showCodeExercises();
                case "codelab" -> showCodeLab();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount("zaleglosci");
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(qv.getView());
        com.example.javademy.storage.DataStorage ds = new com.example.javademy.storage.DataStorage();
        java.util.Set<String> weakIds = com.example.javademy.auth.CurrentUser.getInstance().getStudent().getWeakExerciseIds();
        ds.loadLessons().stream().filter(l -> lessonId.equals(l.getId())).findFirst().ifPresent(lesson -> {
                java.util.List<com.example.javademy.model.QuizExercise> pool =
                    ds.loadQuizExercisesForLesson(lessonId).stream()
                      .filter(q -> weakIds.contains(q.getId()))
                      .collect(java.util.stream.Collectors.toList());
                qv.startQuizForLesson(lesson, pool);
            });
    }

    private void showAccount(String previousView)
    {
        AccountView v = new AccountView();
        v.setPreviousView(previousView);
        v.setOnNavigate(dest -> {
            if (isPrefixed(dest)) {
                handleRepairNav(dest);
                return;
            }
            switch (dest) {
                case "dashboard" -> showDashboard();
                case "lekcje" -> showLessons();
                case "codelab" -> showCodeLab();
                case "zadania" -> showCodeExercises();
                case "quizy" -> showQuiz();
                case "zaleglosci" -> showRepairPlan();
                case "konto" -> showAccount(previousView);
                case "logout" -> logout();
            }
        });
        contentArea.getChildren().setAll(v.getView());
    }

    private void logout()
    {
        CurrentUser.getInstance().logout();

        LoginView loginView = new LoginView(stage);
        javafx.scene.Scene scene = new javafx.scene.Scene(loginView.getView());
        var cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
    }
}
