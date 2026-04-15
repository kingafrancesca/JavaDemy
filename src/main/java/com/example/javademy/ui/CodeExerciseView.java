package com.example.javademy.ui;

import com.example.javademy.ai.ErrorAnalyzer;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.engine.CodeGuard;
import com.example.javademy.engine.CodeRunner;
import com.example.javademy.engine.TestRunner;
import com.example.javademy.model.*;
import com.example.javademy.storage.DataStorage;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Widok zadań programistycznych w aplikacji JavaDemy.
 * Wyświetla listę zadań, umożliwia rozwiązywanie ich w edytorze
 * i sprawdza poprawność przez {@link com.example.javademy.engine.TestRunner}.
 */
public class CodeExerciseView
{
    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BEIGE = "#B5A99A";
    private static final String BORDER_BEIGE = "#DDD0C0";
    private static final String SAGE = "#9FB395";
    private static final String TERRA = "#C4A08A";
    private static final String STAR1 = "#B6866A";
    private static final String START2 = "#E0D5C8";
    private static final String GREEN = "#467D2A";
    private static final String RED = "#B32D26";

    private static final String GLASS_STYLE_ROW = "-fx-background-color: rgba(252,248,243,0.72); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.60); -fx-border-width: 1; -fx-border-radius: 20;";

    private static final String GLASS_STYLE_HEADER = "-fx-background-color: rgba(252,248,243,0.72); -fx-background-radius: 30; -fx-border-color: rgba(255,255,255,0.60); -fx-border-width: 1; -fx-border-radius: 30;";

    private static final String GLASS_STYLE_PANEL = "-fx-background-color: rgba(255,252,248,0.82); -fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.72); -fx-border-width: 1.5; -fx-border-radius: 18;";

    private static final int HINT_UNLOCKED = 3;

    private final DataStorage dataStorage;
    private final ErrorAnalyzer errorAnalyzer;
    private final StackPane view;
    private final Student  student;
    private final String filterLessonId;
    private String openExerciseId;
    private boolean fromRepairPlan = false;
    private int failedAttempts = 0;
    private Consumer<String> onNavigate;

    /** Tworzy widok wszystkich dostępnych zadań kodowych. */
    public CodeExerciseView() {
        this(null, null);
    }

    /**
     * Tworzy widok zadań praktycznych.
     *
     * @param lessonId identyfikator lekcji do filtrowania lub null dla wszystkich zadań
     */
    public CodeExerciseView(String lessonId) {
        this(lessonId, null);
    }

    /**
     * Tworzy widok zadań praktycznych i otwiera wskazane zadanie (tryb RepairPlanView).
     *
     * @param lessonId filtr lekcji lub null
     * @param exerciseId id zadania do automatycznego otwarcia lub null
     */
    public CodeExerciseView(String lessonId, String exerciseId) {
        this.dataStorage = new DataStorage();
        this.student = CurrentUser.getInstance().getStudent();
        this.errorAnalyzer = new ErrorAnalyzer();
        this.view = new StackPane();
        this.filterLessonId = lessonId;
        this.openExerciseId = exerciseId;
        loadFonts();
        showExerciseList();
    }

    /**
     * Ustawia callback nawigacji do innych widoków aplikacji.
     *
     * @param nav funkcja przyjmująca nazwę docelowego widoku
     */
    public void setOnNavigate(Consumer<String> nav) {
        this.onNavigate = nav;
    }

    private void navigate(String dest) {
        if (onNavigate != null) onNavigate.accept(dest);
    }

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

    private void setFont(Label l, double size) {
        l.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, size));
    }

    private void showView(VBox root) {
        VBox bottomBtnGroup = buildBottomButtons();
        StackPane wrapper = new StackPane(root, bottomBtnGroup);
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));
        view.getChildren().setAll(wrapper);
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
        setFont(lbl, 13);
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 8; -fx-border-width: 1;";
        String hov = "-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + TEXT2 + "; -fx-border-radius: 8; -fx-border-width: 1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited(_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }

    private HBox buildNavBar() {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");
        Region sL  = new Region(); HBox.setHgrow(sL,  Priority.ALWAYS);
        Region sM1 = new Region(); HBox.setHgrow(sM1, Priority.ALWAYS);
        Region sM2 = new Region(); HBox.setHgrow(sM2, Priority.ALWAYS);
        Region sR  = new Region(); HBox.setHgrow(sR,  Priority.ALWAYS);

        Label lekcje = navItem("Lekcje",   false);
        Label sep1 = navSep();
        Label codelab = navItem("Code Lab", false);
        lekcje .setOnMouseClicked(_ -> navigate("lekcje"));
        codelab.setOnMouseClicked(_ -> navigate("codelab"));

        Label logo = new Label("JavaDemy");
        logo.setFont(Font.font("Playfair Display", 22));
        logo.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        logo.setOnMouseClicked(_ -> navigate("dashboard"));

        Label zadania = navItem("Zadania", !fromRepairPlan);
        Label sep2 = navSep();
        Label quizy = navItem("Quizy",   false);
        zadania.setOnMouseClicked(_ -> navigate("zadania"));
        quizy.setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private Label navItem(String text, boolean active) {
        Label l = new Label(text);
        setFont(l, 13);
        if (active) l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand; -fx-border-color: transparent transparent " + SAGE + " transparent; -fx-border-width: 0 0 2 0;");
        else {
            l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
            l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
            l.setOnMouseExited(_ -> l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        }
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        setFont(l, 13);
        l.setStyle("-fx-text-fill: " + BORDER_BEIGE + "; -fx-padding: 0 2 0 2;");
        return l;
    }

    private void applyGlassRow(Region node) {
        node.setStyle(GLASS_STYLE_ROW + "-fx-cursor: hand;");
        applyGlassEffect(node);
    }

    private void applyGlassHeader(Region node) {
        node.setStyle(GLASS_STYLE_HEADER + "-fx-cursor: hand;");
        applyGlassEffect(node);
    }

    private void applyGlassPanel(Region node) {
        node.setStyle(GLASS_STYLE_PANEL);
        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 14, 0, -2, -2);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.06),        8, 0,  1,  1);
        lo.setInput(hi);
        node.setEffect(lo);
    }

    private void applyGlassEffect(Region node) {
        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255, 255, 255, 0.30), 12, 0, -1, -1);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.04), 8, 0, 1, 1);
        lo.setInput(hi);
        node.setEffect(lo);
    }

    private HBox buildStars(int difficulty) {
        HBox box = new HBox(3);
        box.setAlignment(Pos.CENTER_RIGHT);
        int filled = Math.min(Math.max(difficulty, 0), 3);
        for (int i = 0; i < 3; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("JetBrains Mono", 11));
            star.setStyle("-fx-text-fill: " + (i < filled ? STAR1 : START2) + ";");
            box.getChildren().add(star);
        }
        return box;
    }

    private void showExerciseList() {
        List<Topic> topics  = dataStorage.loadTopics();
        List<CodeExercise> allCode = dataStorage.loadCodeExercises();

        if (filterLessonId != null)
            allCode = allCode.stream().filter(ce -> filterLessonId.equals(ce.getLessonId())).toList();

        List<AttemptLog> attemptHistory = dataStorage.loadAttempts(student.getLogin());
        Set<String> passedIds = new HashSet<>();
        Set<String> failedIds = new HashSet<>();
        for (AttemptLog a : attemptHistory) {
            if (a.correct()) passedIds.add(a.exerciseId());
            else failedIds.add(a.exerciseId());
        }

        VBox cards = new VBox(14);
        cards.setMaxWidth(820);
        cards.setAlignment(Pos.TOP_CENTER);

        final List<CodeExercise> allCodeFinal = allCode;
        for (Topic topic : topics) {
            List<CodeExercise> topicExs = allCodeFinal.stream().filter(ce -> topic.getId().equals(ce.getTopicId())).collect(Collectors.toList());
            if (topicExs.isEmpty()) continue;
            cards.getChildren().add(buildTopicAccordion(topic, topicExs, passedIds, failedIds));
        }

        StackPane centered = new StackPane(cards);
        StackPane.setAlignment(cards, Pos.TOP_CENTER);
        centered.setPadding(new Insets(8, 40, 60, 40));
        centered.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        Rectangle fadeOverlay = new Rectangle();
        fadeOverlay.setMouseTransparent(true);
        fadeOverlay.widthProperty().bind(scroll.widthProperty());
        fadeOverlay.heightProperty().bind(scroll.heightProperty());
        fadeOverlay.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0,  Color.TRANSPARENT),
            new Stop(0.78, Color.TRANSPARENT),
            new Stop(1.0,  Color.web(BG))));

        StackPane scrollWithFade = new StackPane(scroll, fadeOverlay);
        StackPane.setAlignment(fadeOverlay, Pos.BOTTOM_CENTER);
        VBox.setVgrow(scrollWithFade, Priority.ALWAYS);

        Label title = new Label("Zadania praktyczne");
        setFont(title, 16);
        title.setStyle("-fx-text-fill: " + TEXT2 + ";");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        StackPane titleWrapper = new StackPane(title);
        StackPane.setAlignment(title, Pos.CENTER);
        titleWrapper.setPadding(new Insets(20, 40, 12, 40));
        titleWrapper.setStyle("-fx-background-color: " + BG + ";");

        VBox root = new VBox(0, buildNavBar(), titleWrapper, scrollWithFade);
        root.setStyle("-fx-background-color: " + BG + ";");
        showView(root);

        if (openExerciseId != null) {
            String targetId = openExerciseId;
            openExerciseId = null;
            fromRepairPlan = true;
            List<CodeExercise> allExs = dataStorage.loadCodeExercises();
            allExs.stream().filter(ce -> targetId.equals(ce.getId())).findFirst().ifPresent(found -> {
                    Set<String> weakIds = student.getWeakExerciseIds();
                    List<CodeExercise> lessonExs = allExs.stream().filter(e -> found.getLessonId() != null
                                  && found.getLessonId().equals(e.getLessonId())
                                  && weakIds.contains(e.getId()))
                        .collect(Collectors.toList());
                    openExercise(found, lessonExs);
                });
        }
    }

    private VBox buildTopicAccordion(Topic topic, List<CodeExercise> exercises, Set<String> passedIds, Set<String> failedIds) {
        VBox group = new VBox(0);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(17, 22, 17, 22));
        applyGlassHeader(header);

        Label arrow = new Label("▶");
        setFont(arrow, 11);
        arrow.setStyle("-fx-text-fill: " + BEIGE + ";");
        arrow.setPrefWidth(18);

        Label name = new Label(topic.getName());
        setFont(name, 14);
        name.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        String countText = exercises.size() + " " + (exercises.size() == 1 ? "zadanie" : exercises.size() < 5 ? "zadania" : "zadań");
        Label badge = new Label(countText);
        setFont(badge, 11);
        badge.setStyle("-fx-text-fill: " + BEIGE + "; -fx-background-color: rgba(220,212,200,0.65); -fx-background-radius: 20; -fx-padding: 4 14 4 14;");

        header.getChildren().addAll(arrow, name, headerSpacer, badge);

        VBox subList = new VBox(10);
        subList.setPadding(new Insets(12, 16, 16, 16));
        subList.setVisible(false);
        subList.setManaged(false);
        subList.setStyle("-fx-background-color: rgba(245,240,233,0.35); -fx-background-radius: 0 0 26 26; -fx-border-color: " + SAGE + " transparent transparent transparent; -fx-border-width: 0 0 0 4;");

        for (CodeExercise ce : exercises) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(13, 18, 13, 18));
            applyGlassRow(row);

            String firstLine = ce.getContent().split("\n")[0];
            Label rowTitle = new Label(firstLine);
            setFont(rowTitle, 13);
            rowTitle.setStyle("-fx-text-fill: " + TEXT1 + ";");
            rowTitle.setWrapText(false);
            rowTitle.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
            HBox.setHgrow(rowTitle, Priority.ALWAYS);

            HBox stars = buildStars(ce.getDifficulty());
            stars.setMinWidth(50);

            boolean exPassed = passedIds.contains(ce.getId());
            boolean exFailed = !exPassed && failedIds.contains(ce.getId());
            if (exPassed || exFailed) {
                Circle dot = new Circle(5);
                dot.setFill(Color.web(exPassed ? GREEN : RED));
                DropShadow glow = new DropShadow(6, Color.web(exPassed ? GREEN : RED, 0.5));
                dot.setEffect(glow);
                row.getChildren().add(dot);
            }
            row.getChildren().addAll(rowTitle, stars);
            row.setOnMouseClicked(_ -> openExercise(ce, exercises));

            row.setOnMouseEntered(_ -> row.setStyle("-fx-background-color: rgba(252,248,243,0.92); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.75); -fx-border-width: 1; -fx-border-radius: 20; -fx-cursor: hand;"));
            row.setOnMouseExited(_ -> row.setStyle(GLASS_STYLE_ROW + "-fx-cursor: hand;"));

            subList.getChildren().add(row);
        }

        final boolean[] expanded = {false};
        header.setOnMouseClicked(_ -> {
            expanded[0] = !expanded[0];
            subList.setVisible(expanded[0]);
            subList.setManaged(expanded[0]);

            RotateTransition rot = new RotateTransition(Duration.millis(180), arrow);
            rot.setToAngle(expanded[0] ? 90 : 0);
            rot.play();

            if (expanded[0]) {
                for (int i = 0; i < subList.getChildren().size(); i++) {
                    var card = subList.getChildren().get(i);
                    card.setOpacity(0);
                    card.setTranslateY(-8);
                    FadeTransition fade = new FadeTransition(Duration.millis(220), card);
                    fade.setToValue(1);
                    fade.setDelay(Duration.millis(i * 55L));
                    TranslateTransition slide = new TranslateTransition(Duration.millis(220), card);
                    slide.setToY(0);
                    slide.setDelay(Duration.millis(i * 55L));
                    fade.play(); slide.play();
                }
            }
        });

        group.getChildren().addAll(header, subList);
        return group;
    }

    private void openExercise(CodeExercise ce, List<CodeExercise> group) {
        failedAttempts = 0;
        int idx = group.indexOf(ce) + 1;
        int total = group.size();

        String lessonName = "";
        if (ce.getLessonId() != null) {
            for (Lesson l : dataStorage.loadLessons()) {
                if (l.getId().equals(ce.getLessonId())) {
                    lessonName = l.getTitle();
                    break;
                }
            }
        }

        HBox subNav = new HBox();
        subNav.setAlignment(Pos.CENTER_LEFT);
        subNav.setPadding(new Insets(10, 28, 10, 28));
        subNav.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");

        Label backLbl = new Label("←");
        setFont(backLbl, 12);
        backLbl.setStyle("-fx-text-fill: " + BEIGE + "; -fx-cursor: hand;");
        backLbl.setOnMouseEntered(_ -> backLbl.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand;"));
        backLbl.setOnMouseExited (_ -> backLbl.setStyle("-fx-text-fill: " + BEIGE + "; -fx-cursor: hand;"));
        final boolean wasFromRepair = fromRepairPlan;
        backLbl.setOnMouseClicked(_ -> { if (wasFromRepair) navigate("zaleglosci"); else showExerciseList(); });

        Region sL = new Region(); HBox.setHgrow(sL, Priority.ALWAYS);

        Label titleLbl = new Label("Zadania: " + lessonName);
        setFont(titleLbl, 13);
        titleLbl.setStyle("-fx-text-fill: " + TEXT2 + ";");

        Region sR = new Region(); HBox.setHgrow(sR, Priority.ALWAYS);

        Label posLbl = new Label("Zadanie " + idx + " z " + total);
        setFont(posLbl, 12);
        posLbl.setStyle("-fx-text-fill: " + BEIGE + ";");

        subNav.getChildren().addAll(backLbl, sL, titleLbl, sR, posLbl);

        AnchorPane mainArea = new AnchorPane();
        mainArea.setPadding(new Insets(20, 32, 20, 32));
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        Button[] hintBtnRef = {null};

        VBox leftPanel  = buildLeftPanel(ce, hintBtnRef);
        leftPanel.setPrefWidth(360);
        leftPanel.setMinWidth(260);
        leftPanel.setMaxWidth(420);

        VBox rightPanel = buildRightPanel(ce, hintBtnRef);

        AnchorPane.setTopAnchor(leftPanel,  0.0);
        AnchorPane.setLeftAnchor(leftPanel, 0.0);

        AnchorPane.setTopAnchor(rightPanel,    0.0);
        AnchorPane.setBottomAnchor(rightPanel, 0.0);
        AnchorPane.setLeftAnchor(rightPanel,   380.0);
        AnchorPane.setRightAnchor(rightPanel,  0.0);

        mainArea.getChildren().addAll(leftPanel, rightPanel);

        VBox root = new VBox(0, buildNavBar(), subNav, mainArea);
        VBox.setVgrow(mainArea, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + BG + ";");
        showView(root);
    }

    private VBox buildLeftPanel(CodeExercise ce, Button[] hintBtnRef) {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        applyGlassPanel(panel);

        Label exTitle = new Label(ce.getContent());
        setFont(exTitle, 12);
        exTitle.setStyle("-fx-text-fill: " + TEXT2 + ";");
        exTitle.setWrapText(true);

        HBox diffRow = new HBox(8);
        diffRow.setAlignment(Pos.CENTER_LEFT);
        Label diffLbl = new Label("Trudność:");
        setFont(diffLbl, 11);
        diffLbl.setStyle("-fx-text-fill: " + BEIGE + ";");
        diffRow.getChildren().addAll(diffLbl, buildStars(ce.getDifficulty()));

        panel.getChildren().addAll(exTitle, diffRow);

        if (ce.getHint() != null && !ce.getHint().isBlank()) {
            Button hintBtn = new Button("🔒  Wskazówka");
            hintBtn.setFont(Font.font("JetBrains Mono", 12));
            hintBtn.setDisable(true);
            hintBtn.setMaxWidth(Double.MAX_VALUE);
            hintBtn.setStyle("-fx-background-color: rgba(200,195,188,0.4); -fx-background-radius: 22; -fx-text-fill: " + BEIGE + "; -fx-padding: 10 20; -fx-opacity: 0.55;");
            hintBtnRef[0] = hintBtn;

            VBox hintPanel = new VBox(0);

            hintBtn.setOnAction(_ -> {
                if (!hintPanel.getChildren().isEmpty()) return;
                VBox hintContent = new VBox(8);
                hintContent.setPadding(new Insets(12, 16, 12, 16));
                hintContent.setStyle("-fx-background-color: rgba(159,179,149,0.12); -fx-background-radius: 8; -fx-border-color: " + SAGE + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 8;");
                Label hintText = new Label(ce.getHint());
                hintText.setFont(Font.font("JetBrains Mono", 11));
                hintText.setStyle("-fx-text-fill: " + TEXT2 + ";");
                hintText.setWrapText(true);
                hintContent.getChildren().add(hintText);

                hintContent.setOpacity(0);
                hintContent.setTranslateY(8);
                hintPanel.getChildren().add(hintContent);

                FadeTransition fade  = new FadeTransition(Duration.millis(280), hintContent);
                fade.setToValue(1);
                TranslateTransition slide = new TranslateTransition(Duration.millis(280), hintContent);
                slide.setToY(0);
                fade.play(); slide.play();
                hintBtn.setDisable(true);
            });

            panel.getChildren().addAll(hintBtn, hintPanel);
        }

        return panel;
    }

    private VBox buildRightPanel(CodeExercise ce, Button[] hintBtnRef) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setMaxHeight(Double.MAX_VALUE);
        applyGlassPanel(panel);

        String starter = ce.getStarterCode() != null && !ce.getStarterCode().isBlank() ? ce.getStarterCode() : "public class StudentSolution {\n    public static void main(String[] args) {\n        // Twój kod tutaj\n    }\n}";

        TextArea codeEditor = new TextArea(starter);
        codeEditor.setWrapText(false);
        codeEditor.setPrefRowCount(13);
        codeEditor.setStyle("-fx-control-inner-background: #EAE0D0; -fx-text-fill: " + TEXT1 + "; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
        codeEditor.getStyleClass().add("codelab-area");
        VBox.setVgrow(codeEditor, Priority.ALWAYS);

        int initLines = starter.split("\n", -1).length;
        StringBuilder initLN = new StringBuilder();
        for (int i = 1; i <= initLines; i++) {
            if (i > 1) initLN.append("\n"); initLN.append(i);
        }
        TextArea lineNumbers = new TextArea(initLN.toString());
        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setWrapText(false);
        lineNumbers.setPrefWidth(36);
        lineNumbers.setMinWidth(36);
        lineNumbers.setMaxWidth(36);
        lineNumbers.setStyle("-fx-control-inner-background: #D8D0C4; -fx-text-fill: rgba(90,75,60,0.50); -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
        lineNumbers.getStyleClass().add("codelab-area");
        VBox.setVgrow(lineNumbers, Priority.ALWAYS);

        codeEditor.textProperty().addListener((_, _, text) -> {
            int n = text.split("\n", -1).length;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                if (i > 1) sb.append("\n");
                sb.append(i);
            }
            lineNumbers.setText(sb.toString());
        });

        lineNumbers.skinProperty().addListener((_, _, newSkin) -> {
            if (newSkin != null) {
                javafx.scene.control.ScrollPane ls = (javafx.scene.control.ScrollPane) lineNumbers.lookup(".scroll-pane");
                if (ls != null) {
                    ls.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
                    ls.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
                }
            }
        });
        codeEditor.skinProperty().addListener((_, _, newSkin) -> {
            if (newSkin != null) {
                javafx.scene.control.ScrollPane es = (javafx.scene.control.ScrollPane) codeEditor.lookup(".scroll-pane");
                javafx.scene.control.ScrollPane ls = (javafx.scene.control.ScrollPane) lineNumbers.lookup(".scroll-pane");
                if (es != null && ls != null) es.vvalueProperty().addListener((_, _, nv) -> ls.setVvalue(nv.doubleValue()));
            }
        });

        codeEditor.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.TAB) {
                ev.consume();
                int pos = codeEditor.getCaretPosition();
                codeEditor.insertText(pos, "    ");
            }
            else if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) {
                ev.consume();
                int pos = codeEditor.getCaretPosition();
                String text = codeEditor.getText();
                int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
                String line = text.substring(lineStart, pos);
                StringBuilder indent = new StringBuilder();
                for (char c : line.toCharArray()) {
                    if (c == ' ' || c == '\t') indent.append(c); else break;
                }
                codeEditor.insertText(pos, "\n" + indent);
            }
        });

        Button playBtn = new Button("▶");
        playBtn.setFont(Font.font("JetBrains Mono", 12));
        playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TERRA + "; -fx-cursor: hand;");
        playBtn.setOnMouseEntered(_ -> playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-font-size: 12px;"));
        playBtn.setOnMouseExited(_ -> playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TERRA + ";-fx-cursor: hand;-fx-font-size: 12px;"));

        Label langLabel = new Label("Java");
        setFont(langLabel, 11);
        langLabel.setStyle("-fx-text-fill: " + TEXT2 + ";");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox codeHeader = new HBox(langLabel, headerSpacer, playBtn);
        codeHeader.setAlignment(Pos.CENTER_LEFT);
        codeHeader.setPadding(new Insets(3, 12, 3, 16));

        HBox.setHgrow(codeEditor, Priority.ALWAYS);
        HBox editorWithLines = new HBox(0, lineNumbers, codeEditor);
        VBox.setVgrow(editorWithLines, Priority.ALWAYS);
        VBox editorInner = new VBox(0, editorWithLines);
        editorInner.setPadding(new Insets(10, 12, 10, 12));
        editorInner.setStyle("-fx-background-color: #EAE0D0; -fx-background-radius: 12;");
        InnerShadow edSunkenDark  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.09),       7, 0,  2,  2);
        InnerShadow edSunkenLight = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 5, 0, -1, -1);
        edSunkenLight.setInput(edSunkenDark);
        editorInner.setEffect(edSunkenLight);
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        VBox.setVgrow(editorInner, Priority.ALWAYS);

        VBox editorWrapper = new VBox(0, codeHeader, editorInner);
        editorWrapper.setPadding(new Insets(10, 12, 10, 12));
        editorWrapper.setStyle("-fx-background-color: rgba(245,238,228,0.55); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.45); -fx-border-radius: 16; -fx-border-width: 1;");
        InnerShadow edOuterHL = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.30), 12, 0, -1, -1);
        InnerShadow edOuterDepth = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.04),        8, 0,  1,  1);
        edOuterDepth.setInput(edOuterHL);
        editorWrapper.setEffect(edOuterDepth);
        VBox.setVgrow(editorWrapper, Priority.ALWAYS);

        StackPane editorStack = new StackPane(editorWrapper);
        VBox.setVgrow(editorStack, Priority.ALWAYS);

        Label outputTitle = new Label("Wynik:");
        setFont(outputTitle, 11);
        outputTitle.setStyle("-fx-text-fill: " + BEIGE + ";");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(5);
        outputArea.setStyle("-fx-control-inner-background: #F0E8DC; -fx-text-fill: " + TEXT2 + "; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
        outputArea.getStyleClass().add("codelab-area");

        VBox outputInner = new VBox(0, outputArea);
        outputInner.setPadding(new Insets(10, 12, 10, 12));
        outputInner.setStyle("-fx-background-color: #F0E8DC; -fx-background-radius: 12;");
        InnerShadow outSunkenDark  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.09),       7, 0,  2,  2);
        InnerShadow outSunkenLight = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 5, 0, -1, -1);
        outSunkenLight.setInput(outSunkenDark);
        outputInner.setEffect(outSunkenLight);

        VBox outputWrapper = new VBox(0, outputTitle, outputInner);
        outputWrapper.setPadding(new Insets(10, 12, 10, 12));
        outputWrapper.setSpacing(8);
        outputWrapper.setStyle("-fx-background-color: rgba(245,238,228,0.55); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.45); -fx-border-radius: 16; -fx-border-width: 1;");
        InnerShadow outOuterHL    = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.30), 12, 0, -1, -1);
        InnerShadow outOuterDepth = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.04),        8, 0,  1,  1);
        outOuterDepth.setInput(outOuterHL);
        outputWrapper.setEffect(outOuterDepth);

        Label statusLabel = new Label();
        setFont(statusLabel, 12);
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);

        Button checkBtn = new Button("Sprawdź");
        checkBtn.setFont(Font.font("JetBrains Mono", 12));
        checkBtn.setPrefSize(150, 36);
        checkBtn.setStyle("-fx-background-color: " + SAGE + "; -fx-background-radius: 18; -fx-text-fill: white; -fx-cursor: hand;");
        InnerShadow cbHi  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 8, 0, -2, -2);
        InnerShadow cbSh  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15), 6, 0, 2, 2);
        cbSh.setInput(cbHi);
        DropShadow cbDrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12), 8, 0, 0, 3);
        cbDrop.setInput(cbSh);
        checkBtn.setEffect(cbDrop);
        checkBtn.setOnMouseEntered(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(130), checkBtn); st.setToX(1.04); st.setToY(1.04); st.play();
        });
        checkBtn.setOnMouseExited(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(130), checkBtn); st.setToX(1.0);  st.setToY(1.0);  st.play();
        });

        playBtn.setOnAction(_ -> {
            playBtn.setDisable(true);
            playBtn.setText("…");
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            String code = codeEditor.getText();
            new Thread(() -> {
                CodeGuard.ExecutionResult res = compileAndRun(code);
                javafx.application.Platform.runLater(() -> {
                    String text = res.success() ? (res.output() != null ? res.output() : "(brak wyjścia)") : (res.errorMessage() != null ? res.errorMessage() : "Błąd");
                    outputArea.setText(text);
                    String outColor = res.success() ? "#7A9B6A" : "#9A5B44";
                    outputArea.setStyle("-fx-control-inner-background: #F0E8DC; -fx-text-fill: " + outColor + "; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
                    playBtn.setDisable(false);
                    playBtn.setText("▶");

                    if (!res.success()) {
                        failedAttempts++;
                        if (failedAttempts >= HINT_UNLOCKED && hintBtnRef[0] != null && hintBtnRef[0].isDisable()) unlockHint(hintBtnRef[0]);
                    }
                });
            }).start();
        });

        checkBtn.setOnAction(_ -> {
            String code = codeEditor.getText();
            checkBtn.setDisable(true);
            checkBtn.setText("Sprawdzam…");
            new Thread(() -> {
                TestRunner tr = new TestRunner();
                TestRunner.TestResult result;
                if (ce.getTestCode() != null && !ce.getTestCode().isBlank()) {
                    result = tr.runTests(code, ce.getTestCode());
                }
                else {
                    String expected = ce.getExpectedOutput() != null ? ce.getExpectedOutput() : "";
                    result = tr.check(code, expected);
                }
                final boolean passed = result.passed();
                final String output = result.testOutput() != null ? result.testOutput() : (result.errorMessage() != null ? result.errorMessage() : "Brak wyniku");

                String login = student.getLogin();
                AttemptLog attempt = new AttemptLog(
                    ce.getId(), passed,
                    passed ? new String[]{} : (ce.getErrorTags() != null ? ce.getErrorTags() : new String[]{}),
                    result.errorMessage()
                );
                java.util.List<AttemptLog> attempts = dataStorage.loadAttempts(login);
                attempts.add(attempt);
                dataStorage.saveAttempts(login, attempts);
                errorAnalyzer.analyzeAttempt(attempt, java.util.List.of());

                javafx.application.Platform.runLater(() -> {
                    outputArea.setText(output);
                    outputArea.setStyle("-fx-control-inner-background: #F0E8DC; -fx-text-fill: " + TEXT2 + "; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
                    statusLabel.setText(passed ? "✓  Zadanie zaliczone!" : "✗  Spróbuj jeszcze raz");
                    statusLabel.setStyle("-fx-text-fill: " + (passed ? GREEN : RED) + ";");
                    statusLabel.setVisible(true);
                    statusLabel.setManaged(true);
                    checkBtn.setDisable(false);
                    checkBtn.setText("Sprawdź");

                    if (!passed) {
                        failedAttempts++;
                        if (failedAttempts >= HINT_UNLOCKED && hintBtnRef[0] != null && hintBtnRef[0].isDisable()) unlockHint(hintBtnRef[0]);
                        if (failedAttempts >= HINT_UNLOCKED) {
                            student.addWeakExercise(ce.getId());
                            saveStudentProfile(student);
                        }
                    }
                    else {
                        student.getWeakExercises().remove(ce.getId());
                        saveStudentProfile(student);
                    }
                });
            }).start();
        });

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);
        HBox bottomRow = new HBox(8, statusLabel, bottomSpacer, checkBtn);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(4, 2, 0, 2));

        panel.getChildren().addAll(editorStack, outputWrapper, bottomRow);
        return panel;
    }

    private void unlockHint(Button hintBtn) {
        hintBtn.setDisable(false);
        hintBtn.setText("💡  Wskazówka");
        hintBtn.setStyle("-fx-background-color: " + SAGE + "; -fx-background-radius: 22; -fx-text-fill: white; -fx-padding: 10 20; -fx-opacity: 1.0; -fx-cursor: hand;");
        DropShadow glow = new DropShadow(BlurType.GAUSSIAN, Color.web(SAGE), 10, 0.15, 0, 0);
        hintBtn.setEffect(glow);
    }

    private void saveStudentProfile(Student student) {
        try {
            new com.example.javademy.auth.ProfileManager().saveStudent(student);
        }
        catch (Exception ignored) {}
    }

    private CodeGuard.ExecutionResult compileAndRun(String code) {
        try {
            return new CodeRunner().compileAndRun(code);
        }
        catch (Exception e) {
            return new CodeGuard.ExecutionResult(false, "", "Błąd: " + e.getMessage());
        }
    }

    /** Zwraca główny kontener widoku zadań kodowych. @return kontener widoku zadań */
    public StackPane getView() { return view; }
}
