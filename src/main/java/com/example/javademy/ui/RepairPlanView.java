package com.example.javademy.ui;

import com.example.javademy.auth.CurrentUser;
import com.example.javademy.model.*;
import com.example.javademy.storage.DataStorage;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Widok planu naprawczego w aplikacji JavaDemy.
 * Pokazuje listę lekcji z zaległościami ucznia - quizy do powtórki
 * i zadania kodowe do ponownego rozwiązania.
 */
public class RepairPlanView
{
    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BEIGE = "#B5A99A";
    private static final String SAGE = "#9FB395";
    private static final String TERRA = "#B6866A";
    private static final String BORDER_BEIGE = "#DDD0C0";


    private static final String GLASS_CARD =
        "-fx-background-color: rgba(252,248,243,0.72); -fx-background-radius: 20;-fx-border-color: rgba(255,255,255,0.60); -fx-border-width: 1; -fx-border-radius: 20;";

    private final DataStorage dataStorage;
    private final Student student;
    private final StackPane view;
    private Consumer<String> onNavigate;

    /**
     * Tworzy widok planu naprawczego dla aktualnie zalogowanego ucznia.
     */
    public RepairPlanView()
    {
        this.dataStorage = new DataStorage();
        this.student = CurrentUser.getInstance().getStudent();
        this.view = new StackPane();
        loadFonts();
        buildView();
    }

    /**
     * Ustawia nawigację do innych widoków aplikacji.
     *
     * @param nav funkcja przyjmująca nazwę docelowego widoku
     */
    public void setOnNavigate(Consumer<String> nav) {
        this.onNavigate = nav;
    }

    private void navigate(String dest) {
        if (onNavigate != null) onNavigate.accept(dest);
    }

    /**
     * Zwraca widok planu naprawczego.
     *
     * @return panel główny RepairPlanView
     */
    public StackPane getView() { return view; }

    private void loadFonts() {
        try {
            var a = getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf");
            if (a != null) Font.loadFont(a, 14);
            var b = getClass().getResourceAsStream("/fonts/JetBrainsMono-Bold.ttf");
            if (b != null) Font.loadFont(b, 14);
            var c = getClass().getResourceAsStream("/fonts/PlayfairDisplay-Regular.ttf");
            if (c != null) Font.loadFont(c, 24);
        } catch (Exception ignored) {}
    }

    private void setFont(Label l, String family, double size) {
        l.setFont(Font.font(family, FontWeight.NORMAL, size));
    }

    private void buildView()
    {
        Set<String> weakIds = student.getWeakExerciseIds();

        VBox content;
        if (weakIds.isEmpty()) content = buildEmptyState();
        else content = buildRepairList(weakIds);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        VBox root = new VBox(0, buildNavBar(), scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox bottomBtnGroup = buildBottomButtons();
        Label backArrow = buildBackArrow();
        StackPane wrapper = new StackPane(root, bottomBtnGroup, backArrow);
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));
        StackPane.setAlignment(backArrow, Pos.TOP_LEFT);
        StackPane.setMargin(backArrow, new Insets(81, 0, 0, 24));
        view.getChildren().setAll(wrapper);
    }

    private VBox buildEmptyState()
    {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(100, 40, 60, 40));
        box.setStyle("-fx-background-color: " + BG + ";");

        Label icon = new Label("✓");
        icon.setFont(Font.font("JetBrains Mono", 48));
        icon.setStyle("-fx-text-fill: " + SAGE + ";");

        Label title = new Label("Brak zaległości");
        setFont(title, "Playfair Display", 26);
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Label sub = new Label("Wszystkie quizy i zadania zaliczone. Tak trzymaj!");
        setFont(sub, "JetBrains Mono", 13);
        sub.setStyle("-fx-text-fill: " + TEXT2 + ";");

        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    private VBox buildRepairList(Set<String> weakIds)
    {
        List<Exercise> allExercises = dataStorage.loadExercises();
        List<Lesson>   allLessons   = dataStorage.loadLessons();

        // Zbierz tylko te ćwiczenia, które są w słabych punktach
        List<Exercise> weakExercises = allExercises.stream()
            .filter(ex -> weakIds.contains(ex.getId()))
            .toList();

        // Grupuj po lessonId
        Map<String, List<Exercise>> byLesson = new LinkedHashMap<>();
        for (Exercise ex : weakExercises) {
            String lid = ex.getLessonId() != null ? ex.getLessonId() : "";
            byLesson.computeIfAbsent(lid, _ -> new ArrayList<>()).add(ex);
        }

        // Mapa id -> tytuł lekcji
        Map<String, String> lessonTitles = new HashMap<>();
        for (Lesson l : allLessons) lessonTitles.put(l.getId(), l.getTitle());

        VBox cards = new VBox(16);
        cards.setMaxWidth(820);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.setPadding(new Insets(0, 0, 60, 0));

        for (Map.Entry<String, List<Exercise>> entry : byLesson.entrySet()) {
            String lessonId    = entry.getKey();
            String lessonTitle = lessonTitles.getOrDefault(lessonId, "Lekcja");
            cards.getChildren().add(buildLessonCard(lessonId, lessonTitle, entry.getValue()));
        }

        Label header = new Label("Szlifuj wiedzę");
        setFont(header, "JetBrains Mono", 15);
        header.setStyle("-fx-text-fill: " + TEXT2 + ";");
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);

        int total = weakIds.size();
        Label sub = new Label(total + " " + (total == 1 ? "zaległość" : "zaległości"));
        setFont(sub, "JetBrains Mono", 12);
        sub.setStyle("-fx-text-fill: " + BEIGE + ";");
        sub.setAlignment(Pos.CENTER);
        sub.setMaxWidth(Double.MAX_VALUE);

        StackPane titleBlock = new StackPane();
        titleBlock.setPadding(new Insets(20, 40, 16, 40));
        titleBlock.setStyle("-fx-background-color: " + BG + ";");
        VBox titleBox = new VBox(4, header, sub);
        titleBox.setAlignment(Pos.CENTER);
        titleBlock.getChildren().add(titleBox);

        StackPane centeredCards = new StackPane(cards);
        StackPane.setAlignment(cards, Pos.TOP_CENTER);
        centeredCards.setPadding(new Insets(8, 40, 0, 40));
        centeredCards.setStyle("-fx-background-color: " + BG + ";");

        VBox content = new VBox(0, titleBlock, centeredCards);
        content.setStyle("-fx-background-color: " + BG + ";");
        return content;
    }

    private VBox buildLessonCard(String lessonId, String lessonTitle, List<Exercise> exercises)
    {
        List<QuizExercise> quizzes = exercises.stream()
            .filter(e -> e instanceof QuizExercise)
            .map(e -> (QuizExercise) e)
            .toList();
        List<CodeExercise> codeExs = exercises.stream()
            .filter(e -> e instanceof CodeExercise)
            .map(e -> (CodeExercise) e)
            .toList();

        VBox card = new VBox(0);
        card.setStyle(GLASS_CARD);
        applyGlassEffect(card);

        HBox header = new HBox(10);
        header.setPadding(new Insets(18, 22, 12, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(lessonTitle);
        setFont(titleLbl, "Playfair Display", 16);
        titleLbl.setStyle("-fx-text-fill: " + TEXT1 + ";");
        header.getChildren().add(titleLbl);

        VBox body = new VBox(0);
        body.setPadding(new Insets(0, 18, 16, 18));

        {
            HBox actionRow = new HBox(10);
            actionRow.setAlignment(Pos.CENTER);
            actionRow.setPadding(new Insets(0, 0, 10, 0));

            Button learnBtn = buildActionButton("Ucz się", "#9FB395", "/images/learn.png");
            Button flashBtn = buildActionButton("Fiszki",  "#B6A082", "/images/flashcards.png");
            Button testBtn  = buildActionButton("Test",    "#B07A7A", "/images/test.png");

            learnBtn.setOnAction(_ -> { if (onNavigate != null) onNavigate.accept("lekcja:" + lessonId); });

            if (quizzes.isEmpty()) {
                muteButton(flashBtn);
                muteButton(testBtn);
            } else {
                flashBtn.setOnAction(_ -> { if (onNavigate != null) onNavigate.accept("repair-quiz:" + lessonId); });
                testBtn .setOnAction(_ -> { if (onNavigate != null) onNavigate.accept("quiz-lesson:" + lessonId); });
            }

            actionRow.getChildren().addAll(learnBtn, flashBtn, testBtn);
            body.getChildren().add(actionRow);
        }

        if (!codeExs.isEmpty()) {
            if (!quizzes.isEmpty()) {
                Region divider = new Region();
                divider.setPrefHeight(1);
                divider.setMaxWidth(Double.MAX_VALUE);
                divider.setStyle("-fx-background-color: rgba(220,212,200,0.50);");
                VBox.setMargin(divider, new Insets(4, 0, 10, 0));
                body.getChildren().add(divider);
            }
            Label codeHeader = new Label("Zadania praktyczne");
            setFont(codeHeader, "JetBrains Mono", 11);
            codeHeader.setStyle("-fx-text-fill: " + BEIGE + ";");
            codeHeader.setPadding(new Insets(0, 4, 6, 4));
            body.getChildren().add(codeHeader);
            for (CodeExercise ce : codeExs) {
                body.getChildren().add(buildCodeRow(ce));
            }
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    private Button buildActionButton(String text, String color, String imagePath) {
        ImageView iv = null;
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) {
                iv = new ImageView(new Image(stream));
                iv.setFitWidth(52);
                iv.setFitHeight(52);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }
        } catch (Exception ignored) {}

        Label lbl = new Label(text);
        lbl.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 14));
        lbl.setStyle("-fx-text-fill: " + color + ";");

        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);
        if (iv != null) content.getChildren().add(iv);
        content.getChildren().add(lbl);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setPrefWidth(220);
        btn.setMaxWidth(220);
        btn.setStyle(
            "-fx-background-color: #F0E8DC;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.70);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 16 0;"
        );

        InnerShadow hi   = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.45), 10, 0, -2, -2);
        InnerShadow sh   = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12),        7, 0,  2,  2);
        sh.setInput(hi);
        DropShadow drop  = new DropShadow(BlurType.GAUSSIAN,  Color.rgb(0,0,0,0.11), 10, 0, 0, 4);
        drop.setInput(sh);
        btn.setEffect(drop);

        btn.setOnMouseEntered(_ -> btn.setStyle(
            "-fx-background-color: #F5EDE4;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 16 0;"
        ));
        btn.setOnMouseExited(_ -> btn.setStyle(
            "-fx-background-color: #F0E8DC;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.70);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 16 0;"
        ));
        return btn;
    }

    private void muteButton(Button btn) {
        String mutedStyle =
            "-fx-background-color: #EDE6DC;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(200,192,182,0.40);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: default;" +
            "-fx-padding: 16 0;";
        btn.setStyle(mutedStyle);
        btn.setEffect(null);
        btn.setOpacity(0.45);
        btn.setOnMouseEntered(null);
        btn.setOnMouseExited(null);
    }

    private HBox buildCodeRow(CodeExercise ce)
    {
        String normalStyle =
            "-fx-background-color: rgba(245,240,233,0.50);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.40);" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";
        HBox row = getHBox(ce, normalStyle);

        Label dot = new Label("•");
        dot.setFont(Font.font("JetBrains Mono", 14));
        dot.setStyle("-fx-text-fill: " + TERRA + ";");
        dot.setMinWidth(14);

        Label contentLbl = new Label(ce.getContent());
        setFont(contentLbl, "JetBrains Mono", 12);
        contentLbl.setStyle("-fx-text-fill: " + TEXT1 + ";");
        contentLbl.setWrapText(false);
        HBox.setHgrow(contentLbl, Priority.ALWAYS);
        contentLbl.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(dot, contentLbl);
        VBox.setMargin(row, new Insets(3, 0, 3, 0));
        return row;
    }

    private HBox getHBox(CodeExercise ce, String normalStyle) {
        String hoverStyle =
            "-fx-background-color: rgba(252,248,243,0.85); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.70); -fx-border-radius: 10; -fx-border-width: 1; -fx-cursor: hand;";

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 12, 9, 12));
        row.setStyle(normalStyle);
        row.setOnMouseEntered(_ -> row.setStyle(hoverStyle));
        row.setOnMouseExited (_ -> row.setStyle(normalStyle));
        row.setOnMouseClicked(_ -> {
            if (onNavigate != null) onNavigate.accept("repair-code:" + ce.getId());
        });
        return row;
    }

    private HBox buildNavBar()
    {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle( "-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");

        Region sL  = new Region(); HBox.setHgrow(sL,  Priority.ALWAYS);
        Region sM1 = new Region(); HBox.setHgrow(sM1, Priority.ALWAYS);
        Region sM2 = new Region(); HBox.setHgrow(sM2, Priority.ALWAYS);
        Region sR  = new Region(); HBox.setHgrow(sR,  Priority.ALWAYS);

        Label lekcje  = navItem("Lekcje");
        Label sep1    = navSep();
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
        quizy  .setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private Label navItem(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 13));
        l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
        l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        l.setOnMouseExited (_ -> l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        l.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 13));
        l.setStyle("-fx-text-fill: " + BORDER_BEIGE + "; -fx-padding: 0 2 0 2;");
        return l;
    }

    private VBox buildBottomButtons() {
        Label mojeKonto = bottomBtn("Moje konto");
        mojeKonto.setOnMouseClicked(_ -> navigate("konto"));
        Label wyloguj = bottomBtn("Wyloguj");
        wyloguj.setOnMouseClicked(_ -> navigate("logout"));
        VBox box = new VBox(6, mojeKonto, wyloguj);
        box.setPickOnBounds(false);
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return box;
    }

    private Label bottomBtn(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 13));
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 8; -fx-border-width: 1;";
        String hov = "-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + TEXT2 + "; -fx-border-radius: 8; -fx-border-width: 1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited (_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }

    private Label buildBackArrow() {
        Label lbl = new Label("←");
        lbl.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 14));
        lbl.setPadding(new Insets(6, 12, 6, 12));
        lbl.setStyle("-fx-text-fill: " + BEIGE + "; -fx-cursor: hand;");
        lbl.setOnMouseClicked(_ -> navigate("dashboard"));
        return lbl;
    }

    private void applyGlassEffect(Region node) {
        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255, 255, 255, 0.30), 12, 0, -1, -1);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.04),        8, 0,  1,  1);
        lo.setInput(hi);
        node.setEffect(lo);
    }
}
