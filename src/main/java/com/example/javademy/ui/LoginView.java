package com.example.javademy.ui;

import com.example.javademy.auth.AccountService;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.auth.ProfileManager;
import com.example.javademy.model.Student;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Widok ekranu logowania aplikacji JavaDemy.
 * Wyświetla formularz z polami na login i hasło.
 * Po pomyślnym zalogowaniu przechodzi do {@link MainWindow}.
 */
public class LoginView
{
    private final Stage stage;
    private final AccountService accountService;
    private StackPane view;

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
     * Tworzy widok logowania i buduje jego układ.
     *
     * @param stage główne okno aplikacji.
     */
    public LoginView(Stage stage)
    {
        this.stage = stage;
        this.accountService = new AccountService(new ProfileManager());
        loadFonts();
        buildView();
    }

    private void buildView()
    {
        ImageView bg = new ImageView();
        var bgUrl = getClass().getResource("/images/login-page.png");
        if (bgUrl != null) bg.setImage(new Image(bgUrl.toExternalForm()));

        bg.setPreserveRatio(false);

        int CARD_H   = 310;
        int CARD_W   = 210;
        int PADDING  = 5;

        Label logoLabel = new Label("JavaDemy");
        logoLabel.setStyle("-fx-font-family: 'Playfair Display'; -fx-font-size: 35px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(PADDING, 0, 0, 0));
        logoBox.getChildren().add(logoLabel);

        TextField loginField = field();
        PasswordField passField = passField();
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 9px; -fx-font-family: 'JetBrains Mono';");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        VBox inputBox = new VBox(8, loginField, passField, errorLabel);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(0, PADDING, 0, PADDING));

        Button loginBtn = new Button("Zaloguj się");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(btnPrimary());
        loginBtn.setOnMouseEntered(_ -> loginBtn.setStyle(btnPrimaryHover()));
        loginBtn.setOnMouseExited(_ -> loginBtn.setStyle(btnPrimary()));

        Button regBtn = new Button("Zarejestruj się");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setStyle(btnSecondary());
        regBtn.setOnMouseEntered(_ -> regBtn.setStyle(btnSecondaryHover()));
        regBtn.setOnMouseExited(_ -> regBtn.setStyle(btnSecondary()));

        VBox btnBox = new VBox(7, loginBtn, regBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(0, PADDING, PADDING, PADDING));

        Region spacer1 = new Region();
        spacer1.setPrefHeight(30);
        Region spacer2 = new Region();
        spacer2.setPrefHeight(15);

        VBox card = new VBox(logoBox, spacer1, inputBox, spacer2, btnBox);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: transparent;");
        card.setMaxWidth(CARD_W);
        card.setMaxHeight(CARD_H);
        card.setMinHeight(CARD_H);
        card.setTranslateY(35);

        loginBtn.setOnAction(_ -> {
            String login = loginField.getText().trim();
            String password = passField.getText();
            if (login.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Wypełnij wszystkie pola!");
                errorLabel.setVisible(true);
                return;
            }
            Student student = accountService.login(login, password);
            if (student == null) {
                errorLabel.setText("Nieprawidłowy login lub hasło!");
                errorLabel.setVisible(true);
                passField.clear();
                return;
            }
            CurrentUser.getInstance().setStudent(student);
            new MainWindow(stage).show();
        });
        passField.setOnAction(_ -> loginBtn.fire());
        regBtn.setOnAction(_ -> new RegisterView(stage).show());

        StackPane.setAlignment(card, Pos.CENTER);

        view = new StackPane(bg, card);

        bg.fitWidthProperty().bind(view.widthProperty());
        bg.fitHeightProperty().bind(view.heightProperty());
    }

    private TextField field() {
        TextField f = new TextField();
        f.setPromptText("Login");
        f.setStyle(fieldStyle());
        return f;
    }

    private PasswordField passField() {
        PasswordField f = new PasswordField();
        f.setPromptText("Hasło");
        f.setStyle(fieldStyle());
        return f;
    }

    private String fieldStyle() {
        return "-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 20;" +
                "-fx-border-width: 1; -fx-text-fill: rgba(60,40,20,0.55); -fx-prompt-text-fill: rgba(60,40,20,0.55); -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-padding: 9 17;";
    }

    private String btnPrimary() {
        return "-fx-background-color: #C9977A; -fx-text-fill: #faf3ea; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-background-radius: 20; -fx-padding: 8 0; -fx-cursor: hand;";
    }

    private String btnPrimaryHover() { return btnPrimary().replace("#C9977A","#B8866A"); }

    private String btnSecondary() {
        return "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: #3d2810; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;" +
                "-fx-border-color: rgba(60,40,20,0.3); -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 0; -fx-cursor: hand;";
    }

    private String btnSecondaryHover() {
        return btnSecondary().replace("rgba(255,255,255,0.2)","rgba(255,255,255,0.35)");
    }

    /** Zwraca główny kontener widoku logowania. @return kontener widoku logowania */
    public StackPane getView() {
        return view;
    }
}