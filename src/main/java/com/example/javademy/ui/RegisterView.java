package com.example.javademy.ui;

import com.example.javademy.auth.AccountService;
import com.example.javademy.auth.ProfileManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Widok ekranu rejestracji nowego konta w aplikacji JavaDemy.
 * Wyświetla formularz z polami: imię, login, hasło i powtórzenie hasła,
 * waliduje dane i przekierowuje rejestrację do {@link AccountService}.
 */
public class RegisterView
{
    private final Stage stage;
    private final AccountService accountService;

    /**
     * Tworzy widok rejestracji dla wskazanego okna.
     *
     * @param stage główne okno aplikacji.
     */
    public RegisterView(Stage stage)
    {
        this.stage = stage;
        this.accountService = new AccountService(new ProfileManager());
    }

    private void loadFonts() {
        try {
            var jbm = getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf");
            if (jbm != null) Font.loadFont(jbm, 14);
            var jbmB = getClass().getResourceAsStream("/fonts/JetBrainsMono-Bold.ttf");
            if (jbmB != null) Font.loadFont(jbmB, 14);
            var pd = getClass().getResourceAsStream("/fonts/PlayfairDisplay-Regular.ttf");
            if (pd != null) Font.loadFont(pd, 24);
        } catch (Exception ignored) {}
    }

    /**
     * Buduje i wyświetla scenę rejestracji.
     * Tworzy formularz z walidacją: min. 3 znaki loginu, min. 6 znaków hasła,
     * min. 1 wielka i mała litera, min. 1 znak specjalny,
     * sprawdza zgodność obu haseł i unikalność loginu.
     */
    public void show()
    {
        loadFonts();
        ImageView bg = new ImageView();
        var bgUrl = getClass().getResource("/images/registration-page.png");
        if (bgUrl != null) bg.setImage(new Image(bgUrl.toExternalForm()));

        bg.setPreserveRatio(false);

        int CARD_H = 440;
        int CARD_W = 280;
        int PADDING = 18;

        Label logoLabel = new Label("JavaDemy");
        logoLabel.setStyle("-fx-font-family: 'Playfair Display'; -fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(PADDING, 0, 0, 0));
        logoBox.getChildren().add(logoLabel);

        TextField displayNameField = field("Imię wyświetlane");
        TextField loginField = field("Login");
        PasswordField passField = passField("Hasło");
        PasswordField pass2Field = passField("Powtórz hasło");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 10px; -fx-font-family: 'JetBrains Mono';");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        Label successLabel = new Label("");
        successLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 10px; -fx-font-family: 'JetBrains Mono';");
        successLabel.setWrapText(true);
        successLabel.setVisible(false);

        VBox reqBox = buildPasswordRequirementsVBox(passField);
        reqBox.setVisible(false);
        reqBox.setMouseTransparent(true);
        passField.focusedProperty().addListener((_, _, focused) -> reqBox.setVisible(focused));

        VBox inputBox = new VBox(8, displayNameField, loginField, passField, pass2Field, errorLabel, successLabel);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(22, PADDING, 0, PADDING));

        Button regBtn = new Button("Zarejestruj się");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setStyle(btnPrime());
        regBtn.setOnMouseEntered(_ -> regBtn.setStyle(btnPrimeHover()));
        regBtn.setOnMouseExited (_ -> regBtn.setStyle(btnPrime()));

        Button backBtn = new Button("Masz już konto? Zaloguj się");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setStyle(btnSec());
        backBtn.setOnMouseEntered(_ -> backBtn.setStyle(btnSecHover()));
        backBtn.setOnMouseExited (_ -> backBtn.setStyle(btnSec()));

        VBox btnBox = new VBox(5, regBtn, backBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(0, PADDING, PADDING, PADDING));

        BorderPane card = new BorderPane();
        card.setTop(logoBox);
        card.setCenter(inputBox);
        card.setBottom(btnBox);
        card.setStyle("-fx-background-color: transparent;");
        card.setMaxWidth(CARD_W);
        card.setMaxHeight(CARD_H);
        card.setMinHeight(CARD_H);
        card.setTranslateY(30);

        regBtn.setOnAction(_ -> {
            String displayName = displayNameField.getText().trim();
            String login = loginField.getText().trim();
            String pass = passField.getText();
            String pass2 = pass2Field.getText();

            if (displayName.isEmpty() || login.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Wypełnij wszystkie pola!");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
                return;
            }
            if (login.length() < 3) {
                errorLabel.setText("Login musi mieć min. 3 znaki!");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
                return;
            }
            boolean[] reqs = checkPassReq(pass);
            if (!reqs[0] || !reqs[1] || !reqs[2] || !reqs[3] || !reqs[4]) {
                errorLabel.setText("Hasło nie spełnia wymogów!");
                errorLabel.setVisible(true); successLabel.setVisible(false); return;
            }
            if (!pass.equals(pass2)) {
                errorLabel.setText("Hasła nie są identyczne!");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
                pass2Field.clear();
                return;
            }
            boolean ok = accountService.register(login, pass, displayName);
            if (!ok) {
                errorLabel.setText("Login jest już zajęty!");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
                return;
            }
            errorLabel.setVisible(false);
            regBtn.setDisable(true);
            int[] countdown = {3};
            successLabel.setText("Konto utworzone! Przejście do logowania za " + countdown[0] + "...");
            successLabel.setVisible(true);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
                countdown[0]--;
                if (countdown[0] > 0) {
                    successLabel.setText("Konto utworzone! Przejście do logowania za " + countdown[0] + "...");
                } else {
                    goToLogin();
                }
            }));
            timeline.setCycleCount(3);
            timeline.play();
        });

        backBtn.setOnAction(_ -> goToLogin());

        reqBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setAlignment(reqBox, Pos.CENTER);
        reqBox.setTranslateX(285);
        reqBox.setTranslateY(75);

        StackPane root = new StackPane(bg, card, reqBox);

        bg.fitWidthProperty().bind(root.widthProperty());
        bg.fitHeightProperty().bind(root.heightProperty());

        Scene scene = new Scene(root);
        var cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setScene(scene);
    }

    private void goToLogin() {
        LoginView loginView = new LoginView(stage);

        Scene scene = new Scene(loginView.getView());
        var cssUrl = getClass().getResource("/styles/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        stage.setMaximized(true);
        stage.setScene(scene);
    }

    private boolean[] checkPassReq(String pass)
    {
        return new boolean[] {
            pass.length() >= 6,
            pass.chars().anyMatch(Character::isLowerCase),
            pass.chars().anyMatch(Character::isUpperCase),
            pass.chars().anyMatch(Character::isDigit),
            pass.chars().anyMatch(c -> !Character.isLetterOrDigit(c))
        };
    }

    private VBox buildPasswordRequirementsVBox(PasswordField field)
    {
        String[] texts = {
            "Hasło zawiera min. 6 znaków",
            "Hasło zawiera min. 1 małą literę",
            "Hasło zawiera min. 1 wielką literę",
            "Hasło zawiera min. 1 cyfrę",
            "Hasło zawiera min. 1 znak specjalny"
        };
        Label[] labels = new Label[5];
        String baseStyle = "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 10px;";
        String RED   = "-fx-text-fill: rgba(230,80,80,1.0);";
        String WHITE = "-fx-text-fill: rgba(255,255,255,1.0);";
        for (int i = 0; i < 5; i++) {
            labels[i] = new Label("✗  " + texts[i]);
            labels[i].setStyle(baseStyle + RED);
        }
        VBox box = new VBox(3);
        box.getChildren().addAll(labels);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.28); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.55); -fx-border-radius: 20; -fx-border-width: 1;");
        DropShadow ds = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.20), 18, 0, 0, 4);
        box.setEffect(ds);

        field.textProperty().addListener((_, _, val) -> {
            boolean[] reqs = checkPassReq(val);
            for (int i = 0; i < 5; i++) {
                if (reqs[i]) {
                    labels[i].setText("✓  " + texts[i]);
                    labels[i].setStyle(baseStyle + WHITE);
                } else {
                    labels[i].setText("✗  " + texts[i]);
                    labels[i].setStyle(baseStyle + RED);
                }
            }
        });
        return box;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(fieldStyle());
        return f;
    }

    private PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle(fieldStyle());
        return f;
    }

    private String fieldStyle() {
        return "-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20; -fx-border-width: 1; -fx-text-fill: rgba(60,40,20,0.55); -fx-prompt-text-fill: rgba(60,40,20,0.55); -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-padding: 9 17;";
    }

    private String btnPrime() {
        return "-fx-background-color: #C9977A; -fx-text-fill: #faf3ea; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 0; -fx-cursor: hand;";
    }

    private String btnPrimeHover() {
        return btnPrime().replace("#C9977A","#B8866A");
    }

    private String btnSec() {
        return "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: #3d2810; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-border-color: rgba(60,40,20,0.3); -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 0; -fx-cursor: hand;";
    }

    private String btnSecHover() {
        return btnSec().replace("rgba(255,255,255,0.2)","rgba(255,255,255,0.35)");
    }
}
