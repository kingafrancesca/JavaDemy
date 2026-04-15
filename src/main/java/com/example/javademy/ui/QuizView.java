package com.example.javademy.ui;

import com.example.javademy.ai.ErrorAnalyzer;
import com.example.javademy.ai.ProgressTracker;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.model.*;
import com.example.javademy.storage.DataStorage;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Widok quizów w aplikacji JavaDemy.
 * Wyświetla pytania quizowe z opcjami odpowiedzi, zapisuje wyniki ucznia
 * i przekazuje informacje o błędach do silnika analizy.
 */
public class QuizView
{
    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BEIGE = "#B5A99A";
    private static final String BORDER_BEIGE = "#DDD0C0";
    private static final String SAGE = "#9FB395";
    private static final String TERRA = "#B6866A";
    private static final String GREEN = "#467D2A";
    private static final String RED = "#B32D26";
    private static final String YELLOW = "#BAA859";

    private static final int QUESTIONS_PER_QUIZ = 5;

    private final DataStorage     dataStorage;
    private final ErrorAnalyzer   errorAnalyzer;

    private List<QuizExercise> exercises = new ArrayList<>();
    private List<QuizExercise> lessonPool = new ArrayList<>();
    private List<QuizExercise> wrongInSession = new ArrayList<>();
    private List<QuizExercise> correctInSession = new ArrayList<>();
    private boolean repairMode = false;
    private int  currentIndex = 0;
    private int correctCount = 0;
    private String currentSectionId;
    private Lesson currentLesson;

    private final StackPane view;
    private Consumer<String> onNavigate;

    private final List<HBox> cardPanes   = new ArrayList<>();
    private final List<String> cardOptions = new ArrayList<>();
    private HBox selCard;

    private List<QuizExercise> flashcards = new ArrayList<>();
    private int flashcardIdx   = 0;
    private int flashcardKnow = 0;
    private int flashcardDontKnow = 0;

    /**
     * Tworzy widok quizów dla aktualnie zalogowanego ucznia.
     * Inicjalizuje silnik analizy błędów z historią prób i wyświetla listę lekcji.
     */
    public QuizView()
    {
        this.dataStorage     = new DataStorage();
        this.errorAnalyzer   = new ErrorAnalyzer();
        loadFonts();
        this.view = new StackPane();
        showLessonList();
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
     * Zwraca widok quizów.
     *
     * @return panel główny QuizView
     */
    public StackPane getView() {
        return view;
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

    private void setFont(Labeled l, double size, boolean bold) {
        l.setFont(Font.font("JetBrains Mono", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
    }

    private HBox buildNavBar() { return buildNavBar(true); }

    private HBox buildNavBar(boolean quizyActive) {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle(
            "-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;"
        );
        Region sL  = new Region(); HBox.setHgrow(sL,  Priority.ALWAYS);
        Region sM1 = new Region(); HBox.setHgrow(sM1, Priority.ALWAYS);
        Region sM2 = new Region(); HBox.setHgrow(sM2, Priority.ALWAYS);
        Region sR  = new Region(); HBox.setHgrow(sR,  Priority.ALWAYS);

        Label lekcje  = navItem("Lekcje", false);
        Label sep1    = navSep();
        Label codelab = navItem("Code Lab", false);
        lekcje .setOnMouseClicked(_ -> navigate("lekcje"));
        codelab.setOnMouseClicked(_ -> navigate("codelab"));

        Label logo = new Label("JavaDemy");
        logo.setFont(Font.font("Playfair Display", 22));
        logo.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        logo.setOnMouseClicked(_ -> navigate("dashboard"));

        Label zadania = navItem("Zadania", false);
        Label sep2    = navSep();
        Label quizy   = navItem("Quizy", quizyActive);
        zadania.setOnMouseClicked(_ -> navigate("zadania"));
        if (!quizyActive)
            quizy.setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private Label buildRepairBackArrow() {
        Label lbl = new Label("←");
        setFont(lbl, 14, false);
        lbl.setPadding(new Insets(6, 12, 6, 12));
        lbl.setStyle("-fx-text-fill: " + BEIGE + "; -fx-cursor: hand;");
        lbl.setOnMouseClicked(_ -> {
            repairMode = false; navigate("zaleglosci");
        });
        return lbl;
    }

    private void commitCorrectInSession() {
        if (correctInSession.isEmpty()) return;
        Student student = CurrentUser.getInstance().getStudent();
        for (QuizExercise ex : correctInSession) {
            if (student.isWeakExercise(ex.getId())) student.recordCorrect(ex.getId());
        }
        saveStudentProfile(student);
        correctInSession.clear();
    }

    private Label navItem(String text, boolean active) {
        Label l = new Label(text);
        setFont(l, 13, false);
        if(active) l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand; -fx-border-color: transparent transparent " + SAGE + " transparent; -fx-border-width: 0 0 2 0;");
        else {
            l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
            l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
            l.setOnMouseExited (_ -> l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        }
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        setFont(l, 13, false);
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
        setFont(lbl, 13, false);
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill:" + TEXT2 + ";-fx-cursor:hand;-fx-background-color:" + BG + ";-fx-background-radius:8;-fx-border-color:" + BORDER_BEIGE + ";-fx-border-radius:8;-fx-border-width:1;";
        String hov = "-fx-text-fill:" + TEXT1 + ";-fx-cursor:hand;-fx-background-color:" + BG + ";-fx-background-radius:8;-fx-border-color:" + TEXT2 + ";-fx-border-radius:8;-fx-border-width:1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited (_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }

    private HBox legendDot(String color, String text) {
        Circle dot = new Circle(5, Color.web(color));
        Label lbl = new Label(text);
        setFont(lbl, 11, false);
        lbl.setStyle("-fx-text-fill: " + BEIGE + ";");
        HBox h = new HBox(6, dot, lbl);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private Button createClayButton(String text, String color, double w, double h) {
        Button btn = new Button(text);
        btn.setPrefSize(w, h);
        setFont(btn, 13, false);
        btn.setStyle("-fx-background-color:" + color + "; -fx-background-radius:" + (h / 2) + "; -fx-text-fill:white;-fx-cursor:hand;");

        InnerShadow hi  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 8, 0, -2, -2);
        InnerShadow sh  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15), 6, 0, 2, 2);
        sh.setInput(hi);
        DropShadow drop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12), 8, 0, 0, 4);
        drop.setInput(sh);

        btn.setEffect(drop);
        btn.setOnMouseEntered(_ -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), btn); st.setToX(1.04); st.setToY(1.04); st.play(); drop.setRadius(12); drop.setOffsetY(6); });
        btn.setOnMouseExited (_ -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), btn); st.setToX(1.0);  st.setToY(1.0);  st.play(); drop.setRadius(8);  drop.setOffsetY(4); });
        return btn;
    }

    private static final String CARD_DEF = "-fx-background-color:rgba(252,248,243,0.72); -fx-background-radius:20; -fx-border-color:rgba(255,255,255,0.60); -fx-border-radius:20;-fx-border-width:1;";
    private static final String CARD_SEL = "-fx-background-color:rgba(182,134,106,0.18); -fx-background-radius:20; -fx-border-color:#B6866A;-fx-border-radius:20;-fx-border-width:1.5;";
    private static final String CARD_OK = "-fx-background-color:rgba(159,179,149,0.22); -fx-background-radius:20; -fx-border-color:#9FB395;-fx-border-radius:20;-fx-border-width:1.5;";
    private static final String CARD_ERR = "-fx-background-color:rgba(196,115,110,0.18); -fx-background-radius:20; -fx-border-color:#C4736E;-fx-border-radius:20;-fx-border-width:1.5;";

    private HBox createAnswerCard(String displayText) {
        Label lbl = new Label(displayText);
        setFont(lbl, 13, false);
        lbl.setStyle("-fx-text-fill: " + TEXT2 + ";");
        lbl.setWrapText(true);

        HBox card = new HBox(lbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(CARD_DEF);

        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 14, 0, -2, -2);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.05),        8, 0,  1,  1);
        lo.setInput(hi);
        card.setEffect(lo);

        return card;
    }

    private void highlightCard(HBox card, boolean correct) {
        card.setStyle(correct ? CARD_OK : CARD_ERR);
        card.getChildren().getFirst().setStyle("-fx-text-fill:" + (correct ? GREEN : RED) + ";");
    }

    private void resetAllCards() {
        for (HBox c : cardPanes) {
            c.setStyle(CARD_DEF);
            c.getChildren().getFirst().setStyle("-fx-text-fill:" + TEXT2 + ";");
        }
    }

    private Effect makeArcEffect() {
        InnerShadow hi  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.40), 8, 0, -3, -3);
        InnerShadow sh  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.10), 6, 0, 3, 3);
        sh.setInput(hi);
        DropShadow drop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.10), 8, 0, 4, 5);
        drop.setInput(sh);
        return drop;
    }

    private Effect makeRingEffect() {
        InnerShadow innerDark  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15),      10, 0, -3, -3);
        InnerShadow innerLight = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.80),  8, 0,  3,  3);
        innerLight.setInput(innerDark);
        DropShadow outerDark = new DropShadow(BlurType.GAUSSIAN, Color.rgb(180,150,120,0.50), 10, 0,  4,  5);
        outerDark.setInput(innerLight);
        DropShadow outerLight = new DropShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.90), 10, 0, -4, -5);
        outerLight.setInput(outerDark);
        return outerLight;
    }


    private void showLessonList() {
        Label pageTitle = new Label("Quizy");
        setFont(pageTitle, 16, false);
        pageTitle.setStyle("-fx-text-fill: " + TEXT2 + ";");
        pageTitle.setAlignment(Pos.CENTER);
        pageTitle.setMaxWidth(Double.MAX_VALUE);

        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
            legendDot(SAGE, "Mistrz"),
            legendDot(YELLOW, "Biegły"),
            legendDot(TERRA, "Zaznajomiony"),
            legendDot(RED, "Podjęto próbę"),
            legendDot("#C4B8AA","Nie rozpoczęto")
        );

        VBox header = new VBox(8, pageTitle, legend);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 14, 0));
        header.setMaxWidth(820);
        header.setStyle("-fx-background-color: " + BG + ";");

        StackPane headerWrapper = new StackPane(header);
        StackPane.setAlignment(header, Pos.TOP_CENTER);
        headerWrapper.setPadding(new Insets(0, 40, 0, 40));
        headerWrapper.setStyle("-fx-background-color: " + BG + ";");

        VBox cards = new VBox(7);
        cards.setMaxWidth(820);
        cards.setAlignment(Pos.TOP_CENTER);

        List<Lesson> lessons = dataStorage.loadLessons();
        Student student = CurrentUser.getInstance().getStudent();
        String login = student != null ? student.getLogin() : null;
        Set<String> attemptedExIds = login == null ? new HashSet<>() : dataStorage.loadAttempts(login).stream().map(AttemptLog::exerciseId).collect(Collectors.toSet());

        for (Lesson lesson : lessons) {
            List<String> sIds = ProgressTracker.getSectionIds(lesson);
            List<QuizExercise> lessonExs = dataStorage.loadQuizExercisesForLesson(lesson.getId());
            boolean attempted = lessonExs.stream().anyMatch(ex -> attemptedExIds.contains(ex.getId()));
            double quizScore = student == null ? 0.0 : sIds.stream().mapToDouble(student::getQuizScore).average().orElse(0.0);
            String mastery = getLevelLabel(quizScore, attempted);
            String mColor = getMasteryColor(mastery);

            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(13, 18, 13, 18));
            row.setMaxWidth(Double.MAX_VALUE);
            String rowStyle = "-fx-background-color:rgba(252,248,243,0.72);-fx-background-radius:20;-fx-cursor:hand;-fx-border-color:rgba(255,255,255,0.60);-fx-border-radius:20;-fx-border-width:1;";
            row.setStyle(rowStyle);
            InnerShadow rHi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 14, 0, -2, -2);
            InnerShadow rLo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.06),        8, 0,  1,  1);
            rLo.setInput(rHi);
            row.setEffect(rLo);

            Label nameLabel = new Label(lesson.getTitle());
            setFont(nameLabel, 13, false);
            nameLabel.setStyle("-fx-text-fill: " + TEXT1 + ";");

            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

            Label masteryLabel = new Label(mastery);
            setFont(masteryLabel, 11, false);
            masteryLabel.setStyle("-fx-text-fill: " + mColor + ";");
            masteryLabel.setPrefWidth(130);
            masteryLabel.setAlignment(Pos.CENTER_RIGHT);

            Button startBtn = createClayButton("Start", mColor, 90, 32);
            setFont(startBtn, 11, false);
            final Lesson l = lesson;
            startBtn.setOnAction(_ -> showModeSelectionModal(l));

            row.setOnMouseClicked(_ -> showModeSelectionModal(l));
            row.setOnMouseEntered(_ -> row.setStyle("-fx-background-color:rgba(255,255,255,0.88);-fx-background-radius:20;-fx-cursor:hand;-fx-border-color:rgba(255,255,255,0.80);-fx-border-radius:20;-fx-border-width:1;"));
            row.setOnMouseExited (_ -> row.setStyle(rowStyle));

            row.getChildren().addAll(nameLabel, sp, masteryLabel, startBtn);
            cards.getChildren().add(row);
        }

        Button testBtn = createClayButton("Test", "#523a2c", 150, 42);
        testBtn.setOnAction(_ -> showTestConfig());

        HBox testRow = new HBox(testBtn);
        testRow.setAlignment(Pos.CENTER);
        testRow.setMaxWidth(820);
        testRow.setPadding(new Insets(18, 0, 26, 0));

        VBox contentBox = new VBox(0, testRow, cards);
        contentBox.setMaxWidth(820);
        contentBox.setAlignment(Pos.TOP_CENTER);

        StackPane centered = new StackPane(contentBox);
        StackPane.setAlignment(contentBox, Pos.TOP_CENTER);
        centered.setPadding(new Insets(8, 40, 80, 40));

        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Rectangle fadeOverlay = new Rectangle();
        fadeOverlay.setMouseTransparent(true);
        fadeOverlay.widthProperty().bind(scroll.widthProperty());
        fadeOverlay.heightProperty().bind(scroll.heightProperty());
        fadeOverlay.setFill(new LinearGradient(0,0,0,1,true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.TRANSPARENT), new Stop(0.78, Color.TRANSPARENT), new Stop(1.0, Color.web(BG))));
        StackPane scrollWithFade = new StackPane(scroll, fadeOverlay);
        StackPane.setAlignment(fadeOverlay, Pos.BOTTOM_CENTER);
        VBox.setVgrow(scrollWithFade, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(), headerWrapper, scrollWithFade);
        root.setStyle("-fx-background-color:" + BG + ";");

        VBox bottonBtnGroup = buildBottomButtons();
        StackPane wrapper = new StackPane(root, bottonBtnGroup);
        StackPane.setAlignment(bottonBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottonBtnGroup, new Insets(0,0,20,20));

        view.getChildren().setAll(wrapper);
    }

    private void showTestConfig() {
        Rectangle darker = new Rectangle();
        darker.widthProperty().bind(view.widthProperty());
        darker.heightProperty().bind(view.heightProperty());
        darker.setFill(Color.rgb(0,0,0,0.28));

        double mW = 320, mH = 210, mR = 18;
        Rectangle modalBase = new Rectangle(mW, mH);
        modalBase.setArcWidth(mR*2); modalBase.setArcHeight(mR*2);
        modalBase.setFill(Color.rgb(248,242,234,0.96));
        InnerShadow mHi  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.55), 14, 0, -2, -2);
        InnerShadow mLo  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.05),        8, 0,  1,  1);
        mLo.setInput(mHi);
        DropShadow modalDrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.18), 24, 0, 0, 8);
        modalDrop.setInput(mLo);
        modalBase.setEffect(modalDrop);

        Rectangle modalBorder = new Rectangle(mW, mH);
        modalBorder.setArcWidth(mR*2); modalBorder.setArcHeight(mR*2);
        modalBorder.setFill(Color.TRANSPARENT);
        modalBorder.setStroke(Color.rgb(255,255,255,0.55));
        modalBorder.setStrokeWidth(1);

        Label modalTitle = new Label("Przygotuj test");
        setFont(modalTitle, 15, false);
        modalTitle.setStyle("-fx-text-fill: " + TEXT2 + ";");

        Label questionLbl = new Label("Pytania (max.40)");
        setFont(questionLbl, 13, false);
        questionLbl.setStyle("-fx-text-fill: " + TEXT2 + ";");

        TextField numInput = new TextField("30");
        numInput.setPrefWidth(54); numInput.setMaxWidth(54);
        numInput.setFont(Font.font("JetBrains Mono", 13));
        numInput.setAlignment(Pos.CENTER);
        numInput.setStyle(
            "-fx-background-color:rgba(245,238,228,0.7);" +
            "-fx-background-radius:8;" +
            "-fx-border-color:rgba(255,255,255,0.45);-fx-border-radius:8;-fx-border-width:1;" +
            "-fx-text-fill:" + TEXT2 + ";"
        );

        HBox inputRow = new HBox(12, questionLbl, numInput);
        inputRow.setAlignment(Pos.CENTER);

        Label numError = new Label("Niepoprawna liczba");
        setFont(numError, 11, false);
        numError.setStyle("-fx-text-fill:#c0392b;");
        numError.setVisible(false);

        Button startBtn = createClayButton("Rozpocznij test", SAGE, 180, 40);

        VBox modalContent = new VBox(16, modalTitle, inputRow, numError, startBtn);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(36));

        StackPane modal = new StackPane(new StackPane(modalBase, modalBorder), modalContent);
        ((StackPane)modal.getChildren().getFirst()).setPrefSize(mW, mH);
        ((StackPane)modal.getChildren().getFirst()).setMaxSize(mW, mH);

        modal.setOpacity(0); modal.setScaleX(0.95); modal.setScaleY(0.95);
        darker.setOnMouseClicked(_ -> view.getChildren().removeAll(darker, modal));
        startBtn.setOnAction(_ -> {
            int num;
            try {
                num = Integer.parseInt(numInput.getText().trim());
            } catch (Exception ignored) {
                numError.setVisible(true);
                return;
            }
            if (num < 1 || num > 40) {
                numError.setVisible(true);
                return;
            }
            numError.setVisible(false);
            view.getChildren().removeAll(darker, modal);
            startTotalQuizWithCount(num);
        });

        view.getChildren().addAll(darker, modal);
        FadeTransition  ft = new FadeTransition(Duration.millis(200), modal);  ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), modal); st.setToX(1); st.setToY(1);
        ft.play(); st.play();
    }

    /**
     * Wyświetla modal z wyborem trybu nauki: quiz lub fiszki.
     *
     * @param lesson lekcja, dla której wybierany jest tryb
     */
    private void showModeSelectionModal(Lesson lesson) {
        Rectangle darker = new Rectangle();
        darker.widthProperty().bind(view.widthProperty());
        darker.heightProperty().bind(view.heightProperty());
        darker.setFill(Color.rgb(0,0,0,0.28));

        double mW = 340, mH = 190, mR = 18;
        Rectangle modalBase = new Rectangle(mW, mH);
        modalBase.setArcWidth(mR*2); modalBase.setArcHeight(mR*2);
        modalBase.setFill(Color.rgb(248,242,234,0.96));
        InnerShadow mHi  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.55), 14, 0, -2, -2);
        InnerShadow mLo  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.05),        8, 0,  1,  1);
        mLo.setInput(mHi);
        DropShadow modalDrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.18), 24, 0, 0, 8);
        modalDrop.setInput(mLo);
        modalBase.setEffect(modalDrop);

        Rectangle modalBorder = new Rectangle(mW, mH);
        modalBorder.setArcWidth(mR*2); modalBorder.setArcHeight(mR*2);
        modalBorder.setFill(Color.TRANSPARENT);
        modalBorder.setStroke(Color.rgb(255,255,255,0.55));
        modalBorder.setStrokeWidth(1);

        Label lessonName = new Label(lesson.getTitle());
        setFont(lessonName, 14, false);
        lessonName.setStyle("-fx-text-fill: " + BEIGE + ";");

        Label modalTitle = new Label("Wybierz formę nauki");
        setFont(modalTitle, 12, false);
        modalTitle.setStyle("-fx-text-fill: " + TEXT2 + ";");

        Button quizBtn = createClayButton("Quiz",   SAGE,  130, 42);
        Button flashBtn = createClayButton("Fiszki", TERRA, 130, 42);

        HBox btnRow = new HBox(16, quizBtn, flashBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox modalContent = new VBox(12, lessonName, modalTitle, btnRow);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(36));

        StackPane modal = new StackPane(new StackPane(modalBase, modalBorder), modalContent);
        ((StackPane)modal.getChildren().getFirst()).setPrefSize(mW, mH);
        ((StackPane)modal.getChildren().getFirst()).setMaxSize(mW, mH);

        modal.setOpacity(0); modal.setScaleX(0.95); modal.setScaleY(0.95);
        darker.setOnMouseClicked(_ -> view.getChildren().removeAll(darker, modal));

        quizBtn.setOnAction(_ -> {
            view.getChildren().removeAll(darker, modal);
            startLessonQuiz(lesson);
        });
        flashBtn.setOnAction(_ -> {
            view.getChildren().removeAll(darker, modal);
            startFlashcards(lesson);
        });

        view.getChildren().addAll(darker, modal);
        FadeTransition  ft = new FadeTransition(Duration.millis(200), modal);  ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), modal); st.setToX(1); st.setToY(1);
        ft.play(); st.play();
    }


    private void showCurrentQuestion() {
        QuizExercise ex = exercises.get(currentIndex);
        int total = exercises.size();
        double prog  = (double)(currentIndex + 1) / total;

        double BAR_W = 500;
        Rectangle trackR = new Rectangle(BAR_W, 5);
        trackR.setArcWidth(5); trackR.setArcHeight(5);
        trackR.setFill(Color.web("#E8E1D5"));
        Rectangle fillR = new Rectangle(Math.max(10, BAR_W * prog), 5);
        fillR.setArcWidth(5); fillR.setArcHeight(5);
        fillR.setFill(Color.web(SAGE));
        StackPane barStack = new StackPane(trackR, fillR);
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.setMaxWidth(BAR_W);

        Label questionLbl = new Label("Pytanie " + (currentIndex + 1) + " / " + total);
        setFont(questionLbl, 11, false);
        questionLbl.setStyle("-fx-text-fill: " + BEIGE + ";");

        VBox subNav = new VBox(6, questionLbl, barStack);
        subNav.setAlignment(Pos.CENTER);
        subNav.setPadding(new Insets(12, 40, 12, 40));
        subNav.setStyle("-fx-background-color: " + BG + ";");

        Label questionLabel = new Label(ex.getContent());
        setFont(questionLabel, 16, false);
        questionLabel.setStyle("-fx-text-fill: " + TEXT1 + ";");
        questionLabel.setWrapText(true);
        questionLabel.setAlignment(Pos.CENTER);
        questionLabel.setMaxWidth(600);
        questionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        cardPanes.clear(); cardOptions.clear();
        selCard = null;

        List<String> options = new ArrayList<>(ex.getOptions());
        Collections.shuffle(options);
        String[] letters = {"A", "B", "C", "D"};

        VBox answersBox = new VBox(8);
        answersBox.setMaxWidth(580);
        answersBox.setAlignment(Pos.CENTER);

        final String[] selectedAnswer = {null};

        for (int i = 0; i < options.size(); i++) {
            String opt    = options.get(i);
            String letter = (i < letters.length) ? letters[i] : String.valueOf(i + 1);
            HBox card = createAnswerCard(letter + ".  " + opt);

            cardPanes.add(card);
            cardOptions.add(opt);

            card.setOnMouseClicked(_ -> {
                resetAllCards();
                card.setStyle(CARD_SEL);
                card.getChildren().getFirst().setStyle("-fx-text-fill:" + TERRA + ";");
                selectedAnswer[0] = opt;
                selCard = card;
            });
            answersBox.getChildren().add(card);
        }

        Button submitBtn = createClayButton("Zatwierdź odpowiedź", SAGE, 230, 42);
        Button nextBtn   = createClayButton(
            currentIndex < exercises.size() - 1 ? "Następne pytanie →" : "Zakończ quiz",
            SAGE, 230, 42
        );
        nextBtn.setVisible(false);
        StackPane btnSlot = new StackPane(submitBtn, nextBtn);

        submitBtn.setOnAction(_ -> {
            if (selectedAnswer[0] == null) return;
            handleAnswerNew(ex, selectedAnswer[0], submitBtn, nextBtn);
        });
        nextBtn.setOnAction(_ -> {
            if (currentIndex < exercises.size() - 1) { currentIndex++; showCurrentQuestion(); }
            else showSummary();
        });

        VBox content = new VBox(32, questionLabel, answersBox, btnSlot);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 40, 80, 40));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(!repairMode), subNav, scroll);
        root.setStyle("-fx-background-color:" + BG + ";");
        if (repairMode) {
            Label back = buildRepairBackArrow();
            StackPane wrapper = new StackPane(root, back);
            StackPane.setAlignment(back, Pos.TOP_LEFT);
            StackPane.setMargin(back, new Insets(81, 0, 0, 24));
            view.getChildren().setAll(wrapper);
        }
        else view.getChildren().setAll(root);
    }

    private void handleAnswerNew(QuizExercise ex, String answer, Button submitBtn, Button nextBtn)
    {
        boolean correct = ex.checkAnswer(answer);
        if (correct) correctCount++;

        Student student = CurrentUser.getInstance().getStudent();
        String login = student.getLogin();

        if (!correct) {
            student.addWeakExercise(ex.getId());
            if (repairMode) wrongInSession.add(ex);
        }
        else if (student.isWeakExercise(ex.getId())) {
            if (repairMode) correctInSession.add(ex); // commit later on "Koniec testu"
            else student.recordCorrect(ex.getId());
        }
        saveStudentProfile(student);

        AttemptLog attempt = new AttemptLog(ex.getId(), correct, correct ? new String[]{} : ex.getErrorTags(), null);
        List<AttemptLog> attempts = dataStorage.loadAttempts(login);
        attempts.add(attempt);
        dataStorage.saveAttempts(login, attempts);
        errorAnalyzer.analyzeAttempt(attempt, List.of());

        if (selCard != null) highlightCard(selCard, correct);

        if (!correct) {
            for (int i = 0; i < cardOptions.size(); i++) {
                if (ex.checkAnswer(cardOptions.get(i))) {
                    highlightCard(cardPanes.get(i), true);
                    break;
                }
            }
        }

        for (HBox card : cardPanes) {
            card.setOnMouseClicked(null);
            card.setCursor(javafx.scene.Cursor.DEFAULT);
        }
        submitBtn.setVisible(false);
        nextBtn.setVisible(true);
    }


    private void showSummary() {
        int total   = exercises.size();
        int correct = correctCount;
        double score   = total > 0 ? (double) correct / total : 0.0;
        int percent     = (int)(score * 100);

        Student student = CurrentUser.getInstance().getStudent();
        if (currentSectionId != null) {
            student.updateQuizScore(currentSectionId, score);
            saveStudentProfile(student);
        }
        else if (currentLesson != null) {
            for (String sid : ProgressTracker.getSectionIds(currentLesson)) student.updateQuizScore(sid, score);
            saveStudentProfile(student);
        }

        String mastery = getLevelLabel(score);
        String ringColor = getMasteryColor(mastery);

        Label title = new Label("Quiz ukończony!");
        setFont(title, 22, false);
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");

        StackPane ring = buildResultRing(percent, mastery, ringColor);

        Label scoreLabel = new Label(correct + " / " + total + " poprawnych");
        setFont(scoreLabel, 13, false);
        scoreLabel.setStyle("-fx-text-fill: " + TEXT2 + ";");

        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        if (repairMode) {
            Button endBtn = createClayButton("Koniec testu", TERRA, 200, 42);
            endBtn.setOnAction(_ -> { commitCorrectInSession(); repairMode = false; navigate("zaleglosci"); });

            if (wrongInSession.isEmpty()) {
                Label gratsLbl = new Label("Gratulacje! Umiesz już wszystko");
                setFont(gratsLbl, 14, false);
                gratsLbl.setStyle("-fx-text-fill: " + SAGE + ";");
                gratsLbl.setAlignment(Pos.CENTER);
                buttonsBox.getChildren().addAll(gratsLbl, endBtn);
            }
            else {
                Button retryBtn = createClayButton("Spróbuj ponownie", SAGE, 220, 42);
                List<QuizExercise> retryPool = new ArrayList<>(wrongInSession);
                retryBtn.setOnAction(_ -> {
                    this.exercises = retryPool;
                    this.wrongInSession = new ArrayList<>();
                    Collections.shuffle(this.exercises);
                    currentIndex = 0; correctCount = 0;
                    showCurrentQuestion();
                });
                buttonsBox.getChildren().addAll(retryBtn, endBtn);
            }
        }
        else {
            Button retryBtn = createClayButton("Spróbuj ponownie", SAGE, 220, 42);
            retryBtn.setOnAction(_ -> {
                if (currentLesson != null && !lessonPool.isEmpty()) {
                    Collections.shuffle(lessonPool);
                    exercises = lessonPool.stream().limit(QUESTIONS_PER_QUIZ).collect(Collectors.toList());
                }
                else Collections.shuffle(exercises);
                currentIndex = 0; correctCount = 0;
                showCurrentQuestion();
            });

            Button backBtn = new Button("Wróć do quizów");
            setFont(backBtn, 13, false);
            backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + BEIGE + ";-fx-cursor:hand;-fx-padding:8 0;");
            backBtn.setOnMouseEntered(_ -> backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + TEXT2 + ";-fx-cursor:hand;-fx-padding:8 0;"));
            backBtn.setOnMouseExited (_ -> backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + BEIGE + ";-fx-cursor:hand;-fx-padding:8 0;"));
            backBtn.setOnAction(_ -> showLessonList());

            buttonsBox.getChildren().add(retryBtn);
            if (percent < 100) {
                Button lessonBtn = new Button("Przejdź do lekcji");
                setFont(lessonBtn, 13, false);
                lessonBtn.setStyle("-fx-background-color:transparent;-fx-border-color:" + TERRA + ";-fx-border-radius:21;-fx-border-width:1.5;-fx-background-radius:21;-fx-text-fill:" + TERRA + ";-fx-padding:10 32;-fx-cursor:hand;");
                lessonBtn.setOnMouseEntered(_ -> lessonBtn.setStyle("-fx-background-color:rgba(182,134,106,0.08);-fx-border-color:" + TERRA + ";-fx-border-radius:21;-fx-border-width:1.5;-fx-background-radius:21;-fx-text-fill:" + TERRA + ";-fx-padding:10 32;-fx-cursor:hand;"));
                lessonBtn.setOnMouseExited (_ -> lessonBtn.setStyle("-fx-background-color:transparent;-fx-border-color:" + TERRA + ";-fx-border-radius:21;-fx-border-width:1.5;-fx-background-radius:21;-fx-text-fill:" + TERRA + ";-fx-padding:10 32;-fx-cursor:hand;"));
                lessonBtn.setOnAction(_ -> navigate("lekcje"));
                buttonsBox.getChildren().add(lessonBtn);
            }
            buttonsBox.getChildren().add(backBtn);
        }

        VBox content = new VBox(20, title, ring, scoreLabel, buttonsBox);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 40, 30, 40));
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(!repairMode), content);
        root.setStyle("-fx-background-color:" + BG + ";");
        view.getChildren().setAll(root);
    }

    private StackPane buildResultRing(int percent, String mastery, String ringColor) {
        double SIZE = 190, cx = 95, cy = 95, r = 70, sw = 13;
        double angle = (percent / 100.0) * 360;

        Canvas trackCanvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc1 = trackCanvas.getGraphicsContext2D();
        gc1.setStroke(Color.web("#EDE3D5"));
        gc1.setLineWidth(sw);
        gc1.setLineCap(StrokeLineCap.BUTT);
        gc1.strokeArc(cx - r, cy - r, r*2, r*2, 0, 360, ArcType.OPEN);
        trackCanvas.setEffect(makeRingEffect());

        Canvas arcCanvas = new Canvas(SIZE, SIZE);
        if (angle > 0) {
            GraphicsContext gc2 = arcCanvas.getGraphicsContext2D();
            gc2.setStroke(Color.web(ringColor));
            gc2.setLineWidth(sw);
            gc2.setLineCap(StrokeLineCap.ROUND);
            gc2.strokeArc(cx - r, cy - r, r*2, r*2, 90, -angle, ArcType.OPEN);
            arcCanvas.setEffect(makeArcEffect());
        }

        Label percentLabel = new Label(percent + "%");
        setFont(percentLabel, 28, true);
        percentLabel.setStyle("-fx-text-fill: " + ringColor + ";");

        Label masteryLabel = new Label(mastery);
        setFont(masteryLabel, 12, false);
        masteryLabel.setStyle("-fx-text-fill: " + ringColor + ";");

        VBox labels = new VBox(2, percentLabel, masteryLabel);
        labels.setAlignment(Pos.CENTER);

        StackPane ring = new StackPane(trackCanvas, arcCanvas, labels);
        ring.setAlignment(Pos.CENTER);
        ring.setPrefSize(SIZE, SIZE);
        ring.setMaxSize(SIZE, SIZE);
        return ring;
    }

    /**
     * Uruchamia quiz dla wszystkich pytań z danej lekcji.
     *
     * @param lesson lekcja, dla której uruchamiany jest quiz
     */
    public void startLessonQuiz(Lesson lesson) {
        this.currentLesson = lesson; this.currentSectionId = null;
        List<QuizExercise> pool = dataStorage.loadQuizExercisesForLesson(lesson.getId());

        Student student = CurrentUser.getInstance().getStudent();
        Set<String> wpIds = student.getWeakExerciseIds();

        List<QuizExercise> weakPart  = new ArrayList<>();
        List<QuizExercise> freshPart = new ArrayList<>();
        for (QuizExercise ex : pool) {
            if (wpIds.contains(ex.getId())) weakPart.add(ex);
            else freshPart.add(ex);
        }
        Collections.shuffle(weakPart);
        Collections.shuffle(freshPart);

        List<QuizExercise> selected = new ArrayList<>();
        selected.addAll(weakPart);
        selected.addAll(freshPart);

        this.exercises = selected;
        this.currentIndex = 0; this.correctCount = 0;
        if (!exercises.isEmpty()) showCurrentQuestion();
        else showEmptyMessage("Brak pytań dla tej lekcji.");
    }


    /**
     * Uruchamia quiz powtórkowy z gotową pulą pytań dla wskazanej lekcji.
     *
     * @param lesson lekcja, której dotyczy quiz.
     * @param pool   lista pytań do powtórzenia.
     */
    public void startQuizForLesson(Lesson lesson, List<QuizExercise> pool) {
        this.currentLesson = lesson; this.currentSectionId = null;
        this.repairMode = true;
        this.lessonPool = new ArrayList<>(pool);
        this.wrongInSession = new ArrayList<>();
        this.correctInSession = new ArrayList<>();
        Collections.shuffle(pool);
        this.exercises = new ArrayList<>(pool);
        this.currentIndex = 0; this.correctCount = 0;
        if (!exercises.isEmpty()) showCurrentQuestion();
        else showEmptyMessage("Brak pytań do powtórki dla tej lekcji.");
    }

    private void startTotalQuizWithCount(int maxCount) {
        this.currentSectionId = null; this.currentLesson = null;
        Student student = CurrentUser.getInstance().getStudent();
        List<Lesson> lessons = dataStorage.loadLessons();

        List<QuizExercise> allPool = new ArrayList<>();
        for (Lesson l : lessons) {
            List<String> sids = ProgressTracker.getSectionIds(l);
            if (student != null && student.getLessonProgress(sids) > 0.0)
                allPool.addAll(dataStorage.loadQuizExercisesForLesson(l.getId()));
        }
        if (allPool.isEmpty()) {
            showEmptyMessage("Brak pytań do quizu.\nNajpierw zacznij naukę z dowolnej lekcji!");
            return;
        }

        String login = CurrentUser.getInstance().getStudent().getLogin();
        List<AttemptLog> attempts = dataStorage.loadAttempts(login);
        Map<String,Integer> wrongMap = new HashMap<>(), correctMap = new HashMap<>();
        for (AttemptLog a : attempts) {
            if (a.correct()) correctMap.merge(a.exerciseId(), 1, Integer::sum);
            else wrongMap.merge(a.exerciseId(), 1, Integer::sum);
        }

        List<QuizExercise> wrong = new ArrayList<>(), fresh = new ArrayList<>(), correct = new ArrayList<>();
        for (QuizExercise q : allPool) {
            if (wrongMap.getOrDefault(q.getId(),0) > 0) wrong.add(q);
            else if (correctMap.getOrDefault(q.getId(),0) == 0) fresh.add(q);
            else correct.add(q);
        }
        Collections.shuffle(wrong); Collections.shuffle(fresh); Collections.shuffle(correct);

        int mW = (int)(maxCount * 0.60), mF = (int)(maxCount * 0.30), mC = (int)(maxCount * 0.10);
        List<QuizExercise> selected = new ArrayList<>();
        selected.addAll(wrong.subList(0, Math.min(wrong.size(), mW)));
        selected.addAll(fresh.subList(0, Math.min(fresh.size(), mF)));
        selected.addAll(correct.subList(0, Math.min(correct.size(), mC)));
        if (selected.size() < maxCount) {
            List<QuizExercise> rest = new ArrayList<>(allPool); rest.removeAll(selected);
            Collections.shuffle(rest);
            selected.addAll(rest.subList(0, Math.min(rest.size(), maxCount - selected.size())));
        }
        Collections.shuffle(selected);
        this.exercises = selected; this.currentIndex = 0; this.correctCount = 0;
        showCurrentQuestion();
    }

    private void showEmptyMessage(String msg) {
        Label lbl = new Label(msg);
        setFont(lbl, 13, false);
        lbl.setStyle("-fx-text-fill: " + TEXT2 + ";");
        lbl.setWrapText(true);
        lbl.setAlignment(Pos.CENTER);

        Button back = createClayButton("← Wróć", SAGE, 140, 38);
        back.setOnAction(_ -> showLessonList());

        VBox content = new VBox(20, lbl, back);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(80));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:" + BG + ";-fx-background-color:" + BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(), scroll);
        root.setStyle("-fx-background-color:" + BG + ";");
        view.getChildren().setAll(root);
    }

    /**
     * Uruchamia tryb fiszek dla pytań z danej lekcji.
     *
     * @param lesson lekcja, dla której uruchamiane są fiszki.
     */
    public void startFlashcards(Lesson lesson) {
        this.currentLesson = lesson; this.currentSectionId = null;
        List<QuizExercise> pool = dataStorage.loadQuizExercisesForLesson(lesson.getId());

        Student student = CurrentUser.getInstance().getStudent();
        Set<String> wpIds = student.getWeakExerciseIds();

        this.flashcards = pool.stream()
            .filter(ex -> wpIds.contains(ex.getId()))
            .collect(Collectors.toList());
        Collections.shuffle(this.flashcards);
        this.flashcardIdx = 0;
        this.flashcardKnow = 0;
        this.flashcardDontKnow = 0;

        if (flashcards.isEmpty()) {
            showEmptyMessage("Brak pytań do powtórki dla tej lekcji.");
            return;
        }
        showFlashcard();
    }

    private void showFlashcard() {
        if (flashcardIdx >= flashcards.size()) {
            showFlashcardSummary();
            return;
        }
        QuizExercise ex = flashcards.get(flashcardIdx);
        final boolean[] flipped = {false};

        double BAR_W = 500;
        Rectangle trackR = new Rectangle(BAR_W, 5);
        trackR.setArcWidth(5); trackR.setArcHeight(5);
        trackR.setFill(Color.web("#E8E1D5"));
        double prog = (double)(flashcardIdx + 1) / flashcards.size();
        Rectangle fillR = new Rectangle(Math.max(10, BAR_W * prog), 5);
        fillR.setArcWidth(5); fillR.setArcHeight(5);
        fillR.setFill(Color.web(TERRA));
        StackPane barStack = new StackPane(trackR, fillR);
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.setMaxWidth(BAR_W);

        Label progLabel = new Label("Fishka " + (flashcardIdx + 1) + " / " + flashcards.size());
        setFont(progLabel, 11, false);
        progLabel.setStyle("-fx-text-fill: " + BEIGE + ";");

        VBox subNav = new VBox(6, progLabel, barStack);
        subNav.setAlignment(Pos.CENTER);
        subNav.setPadding(new Insets(12, 40, 12, 40));
        subNav.setStyle("-fx-background-color: " + BG + ";");

        String CARD_BG = "#E2D5C4";
        double CARD_W = 580, CARD_H = 230;

        Rectangle cardBase = new Rectangle(CARD_W, CARD_H);
        cardBase.setArcWidth(48); cardBase.setArcHeight(48);
        cardBase.setFill(Color.web(CARD_BG));
        InnerShadow cHi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.42), 12, 0, -3, -3);
        InnerShadow cLo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12),        8, 0,  3,  3);
        cLo.setInput(cHi);
        DropShadow cDrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.13), 16, 0, 0, 5);
        cDrop.setInput(cLo);
        cardBase.setEffect(cDrop);

        Label questionLbl = new Label(ex.getContent());
        setFont(questionLbl, 15, false);
        questionLbl.setStyle("-fx-text-fill: " + TEXT1 + ";");
        questionLbl.setWrapText(true);
        questionLbl.setMaxWidth(520);
        questionLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        questionLbl.setAlignment(Pos.CENTER);

        Label hintLbl = new Label("Kliknij, aby odkryć odpowiedź");
        setFont(hintLbl, 11, false);
        hintLbl.setStyle("-fx-text-fill: " + BEIGE + ";");

        VBox frontContent = new VBox(14, questionLbl, hintLbl);
        frontContent.setAlignment(Pos.CENTER);
        frontContent.setMaxWidth(CARD_W - 60);

        Label questionSmall = new Label(ex.getContent());
        setFont(questionSmall, 11, false);
        questionSmall.setStyle("-fx-text-fill: " + BEIGE + ";");
        questionSmall.setWrapText(true);
        questionSmall.setMaxWidth(520);
        questionSmall.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        questionSmall.setAlignment(Pos.CENTER);

        Label answerLbl = new Label(ex.getCorrectAnswer());
        setFont(answerLbl, 15, false);
        answerLbl.setStyle("-fx-text-fill: " + GREEN + ";");
        answerLbl.setWrapText(true);
        answerLbl.setMaxWidth(520);
        answerLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        answerLbl.setAlignment(Pos.CENTER);

        VBox backContent = new VBox(10, questionSmall, answerLbl);
        backContent.setAlignment(Pos.CENTER);
        backContent.setMaxWidth(CARD_W - 60);

        StackPane cardPane = new StackPane(cardBase, frontContent);
        cardPane.setPrefSize(CARD_W, CARD_H);
        cardPane.setMaxSize(CARD_W, CARD_H);
        cardPane.setCursor(javafx.scene.Cursor.HAND);

        Button nieUmiemBtn = createClayButton("Nie umiem", RED,  140, 40);
        Button umiemBtn = createClayButton("Umiem",     SAGE, 120, 40);

        HBox ratingBox = new HBox(20, nieUmiemBtn, umiemBtn);
        ratingBox.setAlignment(Pos.CENTER);
        ratingBox.setVisible(false);

        String navStyle = "-fx-background-color:transparent;-fx-text-fill:" + BEIGE + ";-fx-cursor:hand;-fx-padding:8 16 8 16;";
        String navDisabled = "-fx-background-color:transparent;-fx-text-fill:" + BORDER_BEIGE   + ";-fx-cursor:default;-fx-padding:8 16 8 16;";

        Button prevBtn = new Button("← Poprzednia");
        setFont(prevBtn, 13, false);
        prevBtn.setStyle(flashcardIdx == 0 ? navDisabled : navStyle);
        prevBtn.setDisable(flashcardIdx == 0);
        prevBtn.setOnAction(_ -> { flashcardIdx--; showFlashcard(); });

        Button nextBtn = new Button("Następna →");
        setFont(nextBtn, 13, false);
        boolean isLast = flashcardIdx >= flashcards.size() - 1;
        nextBtn.setStyle(isLast ? navDisabled : navStyle);
        nextBtn.setDisable(isLast);
        nextBtn.setOnAction(_ -> { flashcardIdx++; showFlashcard(); });

        cardPane.setOnMouseClicked(_ -> {
            if (flipped[0]) return;
            ScaleTransition out = new ScaleTransition(Duration.millis(150), cardPane);
            out.setFromX(1.0); out.setToX(0.01);
            out.setOnFinished(_ -> {
                cardPane.getChildren().set(1, backContent);
                flipped[0] = true;
                ratingBox.setVisible(true);
                ScaleTransition in = new ScaleTransition(Duration.millis(150), cardPane);
                in.setFromX(0.01); in.setToX(1.0);
                in.play();
            });
            out.play();
        });

        nieUmiemBtn.setOnAction(_ -> {
            flashcardDontKnow++;
            Student student = CurrentUser.getInstance().getStudent();
            student.addWeakExercise(ex.getId());
            saveStudentProfile(student);
            flashcardIdx++;
            showFlashcard();
        });
        umiemBtn.setOnAction(_ -> {
            flashcardKnow++;
            flashcardIdx++;
            showFlashcard();
        });

        VBox cardArea = new VBox(26, cardPane, ratingBox);
        cardArea.setAlignment(Pos.CENTER);
        VBox.setVgrow(cardArea, Priority.ALWAYS);
        cardArea.setStyle("-fx-background-color:" + BG + ";");

        VBox root = new VBox(0, buildNavBar(false), subNav, cardArea);
        root.setStyle("-fx-background-color:" + BG + ";");

        Label back = buildRepairBackArrow();
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(81, 0, 0, 24));

        StackPane.setAlignment(prevBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(prevBtn, new Insets(0, 0, 24, 40));

        StackPane.setAlignment(nextBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(nextBtn, new Insets(0, 40, 24, 0));

        StackPane wrapper = new StackPane(root, back, prevBtn, nextBtn);
        view.getChildren().setAll(wrapper);
    }

    private void showFlashcardSummary() {
        int total = flashcardKnow + flashcardDontKnow;
        int skipped = flashcards.size() - total;

        Label title = new Label("Sesja fishek ukończona!");
        setFont(title, 22, false);
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Label KnowLbl = new Label("Umiem:  " + flashcardKnow);
        setFont(KnowLbl, 14, false);
        KnowLbl.setStyle("-fx-text-fill: " + GREEN + ";");

        Label dontKnowLbl = new Label("Nie umiem:  " + flashcardDontKnow);
        setFont(dontKnowLbl, 14, false);
        dontKnowLbl.setStyle("-fx-text-fill: " + RED + ";");

        VBox statsBox = new VBox(8, KnowLbl, dontKnowLbl);
        statsBox.setAlignment(Pos.CENTER);

        if (skipped > 0) {
            Label skipLbl = new Label("Pominięto:  " + skipped);
            setFont(skipLbl, 12, false);
            skipLbl.setStyle("-fx-text-fill: " + BEIGE + ";");
            statsBox.getChildren().add(skipLbl);
        }

        Button repeatBtn = createClayButton("Powtórz sesję", TERRA, 200, 42);
        repeatBtn.setOnAction(_ -> startFlashcards(currentLesson));

        Button backBtn = new Button("Wróć do planu naprawczego");
        setFont(backBtn, 13, false);
        backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + BEIGE + ";-fx-cursor:hand;-fx-padding:8 0;");
        backBtn.setOnMouseEntered(_ -> backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + TEXT2 + ";-fx-cursor:hand;-fx-padding:8 0;"));
        backBtn.setOnMouseExited (_ -> backBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + BEIGE + ";-fx-cursor:hand;-fx-padding:8 0;"));
        backBtn.setOnAction(_ -> navigate("zaleglosci"));

        VBox content = new VBox(24, title, statsBox, repeatBtn, backBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60, 40, 40, 40));
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(false), content);
        root.setStyle("-fx-background-color:" + BG + ";");
        Label back = buildRepairBackArrow();
        StackPane wrapper = new StackPane(root, back);
        StackPane.setAlignment(back, Pos.TOP_RIGHT);
        StackPane.setMargin(back, new Insets(10, 24, 0, 0));
        view.getChildren().setAll(wrapper);
    }

    private void saveStudentProfile(Student student) {
        try {
            new com.example.javademy.auth.ProfileManager().saveStudent(student);
        }
        catch (Exception e) {
            System.err.println("Błąd zapisu profilu: " + e.getMessage());
        }
    }

    private String getLevelLabel(double score, boolean attempted) {
        if (!attempted)   return "Nie rozpoczęto";
        if (score < 0.30) return "Podjęto próbę";
        if (score < 0.70) return "Zaznajomiony";
        if (score < 1.0)  return "Biegły";
        return "Mistrz 👑";
    }

    private String getLevelLabel(double score) {
        return getLevelLabel(score, true);
    }

    private String getMasteryColor(String mastery) {
        return switch (mastery) {
            case "Mistrz 👑"    -> SAGE;
            case "Biegły"       -> YELLOW;
            case "Zaznajomiony" -> TERRA;
            case "Podjęto próbę"-> RED;
            default             -> "#C4B8AA";
        };
    }
}
