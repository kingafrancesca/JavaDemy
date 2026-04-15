package com.example.javademy.ui;

import com.example.javademy.auth.AccountService;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.auth.ProfileManager;
import com.example.javademy.model.Student;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * Widok konta użytkownika w aplikacji JavaDemy.
 */
public class AccountView
{
    private final StackPane view;
    private Consumer<String> onNavigate;
    private final AccountService accountService;
    private String previousView = "dashboard";

    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BEIGE = "#B0A494";
    private static final String BORDER_BEIGE   = "#DDD0C0";
    private static final String BTN_ACTION  = "#C9977A";
    private static final String BTN_CONFIRM = "#9FB395";

    private static final String GLASS_CARD = "-fx-background-color: rgba(255,255,255,0.38); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.72); -fx-border-width: 1; -fx-border-radius: 20;";

    /** Tworzy widok konta i buduje jego układ. */
    public AccountView()
    {
        accountService = new AccountService(new ProfileManager());
        view = new StackPane();
        view.setStyle("-fx-background-color: " + BG + ";");
        showMode(false);
    }

    /**
     * Ustawia callback nawigacji do innych widoków.
     * @param handler funkcja przyjmująca nazwę docelowego widoku
     */
    public void setOnNavigate(Consumer<String> handler) { this.onNavigate = handler; }

    /**
     * Ustawia widok, do którego prowadzi strzałka powrotu.
     * @param prev nazwa widoku powrotu
     */
    public void setPreviousView(String prev) { this.previousView = prev; }

    /**
     * Zwraca główny kontener widoku.
     * @return kontener widoku konta
     */
    public StackPane getView() { return view; }

    private void navigate(String dest) {
        if (onNavigate != null) onNavigate.accept(dest);
    }

    private void showMode(boolean editMode)
    {
        Student student = CurrentUser.getInstance().getStudent();

        Label title = new Label("Moje konto");
        title.setFont(Font.font("Playfair Display", 28));
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");
        title.setPadding(new Insets(22, 0, 16, 0));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        VBox card = buildCard(student, editMode);
        card.setMaxWidth(460);

        VBox cardWrapper = new VBox(card);
        cardWrapper.setAlignment(Pos.TOP_CENTER);
        cardWrapper.setPadding(editMode ? new Insets(0, 40, 28, 40) : new Insets(0, 40, 40, 40));

        StackPane centeredStack = new StackPane(cardWrapper);
        centeredStack.setStyle("-fx-background-color: " + BG + ";");
        centeredStack.setAlignment(editMode ? Pos.TOP_CENTER : Pos.CENTER);

        ScrollPane scroll = new ScrollPane(centeredStack);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(!editMode);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        VBox root = new VBox(0, buildNavBar(), title, scroll);
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setFocusTraversable(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label backArrow = buildBackArrow();
        VBox bottomBtnGroup = buildBottomButtons();

        StackPane wrapper = new StackPane(root, backArrow, bottomBtnGroup);
        StackPane.setAlignment(backArrow, Pos.TOP_LEFT);
        StackPane.setMargin(backArrow, new Insets(81, 0, 0, 24));
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));

        view.getChildren().setAll(wrapper);
        Platform.runLater(root::requestFocus);
    }

    private VBox buildCard(Student student, boolean editMode)
    {
        VBox card = new VBox(editMode ? 6 : 8);
        card.setStyle(GLASS_CARD);
        card.setPadding(new Insets(16, 28, 16, 28));

        DropShadow cardShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(100, 75, 50, 0.10), 20, 0, 0, 5);
        card.setEffect(cardShadow);

        if (!editMode) {
            TextField nameField  = readonlyField(student.getDisplayName());
            TextField loginField = readonlyField(student.getLogin());
            PasswordField passField = new PasswordField();
            passField.setText("xxxxxxxx");
            passField.setEditable(false);
            passField.setStyle(inputStyle());

            card.getChildren().addAll(
                labeledField("Imię",  nameField),
                labeledField("Login", loginField),
                labeledField("Hasło", passField)
            );
            Button editBtn = createClayButton("Zmień dane", BTN_ACTION);
            VBox.setMargin(editBtn, new Insets(10, 0, 0, 0));
            editBtn.setOnAction(_ -> showMode(true));
            card.getChildren().add(editBtn);
        }
        else {
            TextField nameField  = inputField(student.getDisplayName());
            TextField loginField = inputField(student.getLogin());
            loginField.setDisable(true);
            loginField.setOpacity(0.55);

            PasswordField oldPassField = passField();
            PasswordField newPassField = passField();
            PasswordField repPassField = passField();

            Label errorLabel = new Label("");
            errorLabel.setStyle("-fx-text-fill: #C0392B; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            errorLabel.setWrapText(true);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            buildPasswordRequirementsPopup(newPassField);

            card.getChildren().addAll(
                labeledField("Imię", nameField),
                labeledField("Login", loginField),
                labeledField("Stare hasło", oldPassField),
                labeledField("Nowe hasło", newPassField),
                labeledField("Powtórz hasło", repPassField),
                errorLabel
            );

            Button saveBtn = createClayButton("Zaakceptuj zmiany", BTN_CONFIRM);
            Button cancelBtn = createClayButton("Anuluj", BTN_ACTION);
            VBox.setMargin(saveBtn, new Insets(14, 0, 0, 0));

            saveBtn.setOnAction(_ -> handleSave(
                nameField.getText(),
                oldPassField.getText(),
                newPassField.getText(),
                repPassField.getText(),
                errorLabel,
                student
            ));
            cancelBtn.setOnAction(_ -> showMode(false));

            card.getChildren().addAll(saveBtn, cancelBtn);
        }

        return card;
    }

    private boolean[] checkPasswordRequirements(String pass)
    {
        return new boolean[] {
            pass.length() >= 6,
            pass.chars().anyMatch(Character::isLowerCase),
            pass.chars().anyMatch(Character::isUpperCase),
            pass.chars().anyMatch(Character::isDigit),
            pass.chars().anyMatch(c -> !Character.isLetterOrDigit(c))
        };
    }

    private void buildPasswordRequirementsPopup(PasswordField field)
    {
        String[] texts = {
            "Hasło zawiera min. 6 znaków",
            "Hasło zawiera min. 1 małą literę",
            "Hasło zawiera min. 1 wielką literę",
            "Hasło zawiera min. 1 cyfrę",
            "Hasło zawiera min. 1 znak specjalny"
        };
        Label[] labels = new Label[5];
        String baseStyle = "-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;";
        for (int i = 0; i < 5; i++) {
            labels[i] = new Label("✗  " + texts[i]);
            labels[i].setStyle(baseStyle + "-fx-text-fill: #c0392b;");
        }
        VBox content = new VBox(5);
        content.getChildren().addAll(labels);
        content.setPadding(new Insets(12, 16, 12, 16));
        content.setStyle("-fx-background-color: rgba(255,255,255,0.55); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.75); -fx-border-radius: 20; -fx-border-width: 1;");
        DropShadow ds = new DropShadow(BlurType.GAUSSIAN, Color.rgb(100, 75, 50, 0.14), 18, 0, 0, 4);
        content.setEffect(ds);

        Popup popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(false);

        field.textProperty().addListener((_, _, val) -> {
            boolean[] reqs = checkPasswordRequirements(val);
            for (int i = 0; i < 5; i++) {
                if (reqs[i]) {
                    labels[i].setText("✓  " + texts[i]);
                    labels[i].setStyle(baseStyle + "-fx-text-fill: " + BTN_CONFIRM + ";");
                }
                else {
                    labels[i].setText("✗  " + texts[i]);
                    labels[i].setStyle(baseStyle + "-fx-text-fill: #c0392b;");
                }
            }
        });

        field.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                Bounds b = field.localToScreen(field.getBoundsInLocal());
                if (b != null) popup.show(field, b.getMaxX() + 14, b.getMinY() - 4);
            }
            else popup.hide();
        });

        field.sceneProperty().addListener((_, _, sc) -> {
            if (sc == null) popup.hide();
        });
    }

    private void handleSave(String newName, String oldPass, String newPass, String repPass,
                             Label errorLabel, Student student)
    {
        if (newName.isBlank()) {
            showError(errorLabel, "Imię nie może być puste!");
            return;
        }
        boolean changingPass = !oldPass.isEmpty() || !newPass.isEmpty() || !repPass.isEmpty();
        if (changingPass) {
            if (oldPass.isEmpty() || newPass.isEmpty() || repPass.isEmpty()) {
                showError(errorLabel, "Wypełnij wszystkie pola hasła!");
                return;
            }
            if (!accountService.verifyPassword(student.getPasswordHash(), oldPass)) {
                showError(errorLabel, "Stare hasło jest nieprawidłowe!");
                return;
            }
            boolean[] reqs = checkPasswordRequirements(newPass);
            if (!reqs[0] || !reqs[1] || !reqs[2] || !reqs[3] || !reqs[4]) {
                showError(errorLabel, "Hasło nie spełnia wymogów!");
                return;
            }
            if (!newPass.equals(repPass)) {
                showError(errorLabel, "Nowe hasła nie są identyczne!");
                return;
            }
        }
        accountService.updateProfile(student, newName.trim(), changingPass ? newPass : null);
        showSuccessPopup();
    }

    private void showError(Label errorLabel, String msg)
    {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccessPopup()
    {
        Label msg = new Label("Dane zmienione poprawnie.");
        msg.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 14));
        msg.setStyle("-fx-background-color: rgba(252,248,243,0.95); -fx-text-fill: " + TEXT1 + "; -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.80); -fx-border-radius: 16; -fx-border-width: 1; -fx-padding: 18 32;");
        DropShadow ds = new DropShadow(BlurType.GAUSSIAN, Color.rgb(100, 75, 50, 0.18), 24, 0, 0, 6);
        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.90), 12, 0, -2, -2);
        hi.setInput(ds);
        msg.setEffect(hi);

        Region dimmer = new Region();
        dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.12);");

        StackPane popupLayer = new StackPane(dimmer, msg);
        popupLayer.setMouseTransparent(true);
        StackPane.setAlignment(msg, Pos.CENTER);

        view.getChildren().add(popupLayer);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(_ -> {
            view.getChildren().remove(popupLayer);
            showMode(false);
        });
        pause.play();
    }

    private TextField readonlyField(String value)
    {
        TextField f = new TextField(value);
        f.setEditable(false);
        f.setStyle(inputStyle());
        return f;
    }

    private VBox labeledField(String label, Control field)
    {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 11));
        lbl.setStyle("-fx-text-fill: " + BEIGE + ";");
        return new VBox(3, lbl, field);
    }

    private TextField inputField(String value)
    {
        TextField f = new TextField(value);
        f.setStyle(inputStyle());
        return f;
    }

    private PasswordField passField()
    {
        PasswordField f = new PasswordField();
        f.setPromptText("");
        f.setStyle(inputStyle());
        return f;
    }

    private String inputStyle()
    {
        return "-fx-background-color: rgba(255,255,255,0.60); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.65); -fx-border-radius: 10; -fx-border-width: 1; -fx-text-fill: " + TEXT1 + "; -fx-prompt-text-fill: " + BEIGE + "; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-padding: 6 12;";
    }

    private Button createClayButton(String text, String bgColor)
    {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 13));
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 20;");
        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 8, 0, -2, -2);
        InnerShadow sh = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15), 6, 0, 2, 2);
        sh.setInput(hi);
        DropShadow drop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12), 8, 0, 0, 4);
        drop.setInput(sh);
        btn.setEffect(drop);
        btn.setOnMouseEntered(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.03); st.setToY(1.03); st.play();
            drop.setRadius(12); drop.setOffsetY(6);
        });
        btn.setOnMouseExited(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0); st.setToY(1.0); st.play();
            drop.setRadius(8); drop.setOffsetY(4);
        });
        return btn;
    }

    private Label buildBackArrow()
    {
        Label l = new Label("←");
        l.setFont(Font.font("JetBrains Mono", 16));
        l.setPadding(new Insets(4, 8, 4, 4));
        l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-cursor: hand;");
        l.setOnMouseClicked(_ -> navigate(previousView));
        return l;
    }

    private HBox buildNavBar()
    {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");

        Region sL  = new Region(); HBox.setHgrow(sL,  Priority.ALWAYS);
        Region sM1 = new Region(); HBox.setHgrow(sM1, Priority.ALWAYS);
        Region sM2 = new Region(); HBox.setHgrow(sM2, Priority.ALWAYS);
        Region sR  = new Region(); HBox.setHgrow(sR,  Priority.ALWAYS);

        Label lekcje = navItem("Lekcje");
        Label sep1 = navSep();
        Label codelab = navItem("Code Lab");
        lekcje .setOnMouseClicked(_ -> navigate("lekcje"));
        codelab.setOnMouseClicked(_ -> navigate("codelab"));

        Label logo = new Label("JavaDemy");
        logo.setFont(Font.font("Playfair Display", 22));
        logo.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        logo.setOnMouseClicked(_ -> navigate("dashboard"));

        Label zadania = navItem("Zadania");
        Label sep2 = navSep();
        Label quizy = navItem("Quizy");
        zadania.setOnMouseClicked(_ -> navigate("zadania"));
        quizy.setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private Label navItem(String text)
    {
        Label l = new Label(text);
        l.setFont(Font.font("JetBrains Mono", 13));
        l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
        l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        l.setOnMouseExited (_ -> l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        return l;
    }

    private Label navSep()
    {
        Label l = new Label("|");
        l.setFont(Font.font("JetBrains Mono", 13));
        l.setStyle("-fx-text-fill: " + BORDER_BEIGE + "; -fx-padding: 0 2 0 2;");
        return l;
    }

    private VBox buildBottomButtons()
    {
        Label mojeKonto = bottomBtn("Moje konto");
        mojeKonto.setOnMouseClicked(_ -> navigate("konto"));
        Label wyloguj = bottomBtn("Wyloguj");
        wyloguj.setOnMouseClicked(_ -> navigate("logout"));
        VBox box = new VBox(6, mojeKonto, wyloguj);
        box.setPickOnBounds(false);
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return box;
    }

    private Label bottomBtn(String text)
    {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 13));
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 8; -fx-border-width: 1;";
        String hov = "-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + TEXT2 + "; -fx-border-radius: 8; -fx-border-width: 1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited (_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }
}
