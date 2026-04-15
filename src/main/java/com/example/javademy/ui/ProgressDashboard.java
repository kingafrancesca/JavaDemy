package com.example.javademy.ui;
import com.example.javademy.ai.ProgressTracker;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.model.Student;
import com.example.javademy.model.Topic;
import com.example.javademy.storage.DataStorage;
import javafx.animation.*;
import javafx.scene.canvas.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard postępów ucznia w aplikacji JavaDemy.
 * Wyświetla powitanie i wykres postępów
 * oraz pasek nawigacji do pozostałych widoków aplikacji.
 */
public class ProgressDashboard
{
    private final VBox root;
    private final ScrollPane scroll;
    private final Student student;
    private final DataStorage dataStorage;
    private final Consumer<String> onNavigate;
    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String LIGHT_RED = "#E2B0A3";
    private static final String BORDER_BEIGE = "#DDD0C0";

    /**
     * Tworzy dashboard i buduje jego układ: pasek nawigacji, sekcję z przywitaniem i sekcję postępów.
     *
     * @param onNavigate funkcja zwracająca nazwę widoku docelowego
     */
    public ProgressDashboard(Consumer<String> onNavigate)
    {
        this.student = CurrentUser.getInstance().getStudent();
        this.dataStorage = new DataStorage();
        this.onNavigate = onNavigate;
        loadFonts();

        VBox content = new VBox(0);
        content.getChildren().addAll(buildComputerArea(), buildProgressSection());

        scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-background: " + BG + ";");

        root = new VBox(0);
        root.getChildren().addAll(buildNavBar(), scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color: " + BG + ";");
    }

    private void setFont(Label label, String family, double size, boolean bold) {
        FontWeight w = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        label.setFont(Font.font(family, w, size));
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

    private HBox buildNavBar() {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle("-fx-background-color: " + BG + ";" +
                "-fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent;" +
                "-fx-border-width: 0 0 1 0;");

        Region spacerL = new Region(); HBox.setHgrow(spacerL, Priority.ALWAYS);
        Label lekcje = navItem("Lekcje");
        Label sep1 = navSep();
        Label codelab = navItem("Code Lab");
        lekcje.setOnMousePressed(_  -> {
            if (onNavigate != null) onNavigate.accept("lekcje");
        });
        codelab.setOnMousePressed(_ -> {
            if (onNavigate != null) onNavigate.accept("codelab");
        });

        Region spacerM1 = new Region(); HBox.setHgrow(spacerM1, Priority.ALWAYS);
        Label logo = new Label("JavaDemy");
        setFont(logo, "Playfair Display", 22, false);
        logo.setStyle("-fx-text-fill: " + TEXT1 + ";");
        Region spacerM2 = new Region(); HBox.setHgrow(spacerM2, Priority.ALWAYS);

        Label zadania = navItem("Zadania");
        Label sep2 = navSep();
        Label quizy = navItem("Quizy");
        zadania.setOnMousePressed(_ -> {
            if (onNavigate != null) onNavigate.accept("zadania"); });
        quizy.setOnMousePressed(_   -> {
            if (onNavigate != null) onNavigate.accept("quizy"); });
        Region spacerR = new Region(); HBox.setHgrow(spacerR, Priority.ALWAYS);

        nav.getChildren().addAll(spacerL, lekcje, sep1, codelab,
                spacerM1, logo, spacerM2, zadania, sep2, quizy, spacerR);
        return nav;
    }

    private StackPane buildComputerArea() {
        StackPane area = new StackPane();
        area.setStyle("-fx-background-color: " + BG + ";");
        area.setPadding(new Insets(40, 0, 0, 0));
        area.setMinHeight(500);

        ImageView computer = new ImageView();
        var compUrl = getClass().getResource("/images/computer_3d.png");
        if (compUrl != null) computer.setImage(new Image(compUrl.toExternalForm()));
        computer.setFitWidth(500);
        computer.setPreserveRatio(true);
        computer.setSmooth(true);
        computer.setMouseTransparent(true);

        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.GAUSSIAN);
        shadow.setRadius(55);
        shadow.setSpread(0.0);
        shadow.setOffsetY(8);
        shadow.setOffsetX(0);
        shadow.setColor(Color.rgb(100, 75, 50, 0.22));

        computer.setEffect(shadow);

        String name = student != null ? student.getDisplayName() : "Student";
        Label screenText = new Label();
        setFont(screenText, "JetBrains Mono", 11, true);
        screenText.setStyle("-fx-text-fill: #7CB87A; -fx-wrap-text: true; -fx-max-width: 200px;");
        screenText.setTranslateY(-125);
        screenText.setTranslateX(3);
        screenText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.web("#AAFFAA", 0.7), 8, 0, 0, 0));

        area.getChildren().addAll(computer, screenText);
        area.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) startTypingAnimation(screenText, name);
        });
        return area;
    }

    private void startTypingAnimation(Label label, String name) {
        String full = "System.out.println(\"Witaj, " + name + "!\");";
        label.setText("");
        Timeline t = new Timeline();
        for (int i = 1; i <= full.length(); i++) {
            final String partial = full.substring(0, i);
            t.getKeyFrames().add(new KeyFrame(Duration.millis(i * 55L), _ -> label.setText(partial)));
        }
        t.setOnFinished(_ -> startGlowPulse(label));
        t.play();
    }

    private void startGlowPulse(Label label) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FFF4D1"));
        glow.setRadius(14);
        label.setEffect(glow);
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 14, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1.8), new KeyValue(glow.radiusProperty(), 26, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(3.6), new KeyValue(glow.radiusProperty(), 14, Interpolator.EASE_BOTH))
        );
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private VBox buildProgressSection() {
        VBox section = new VBox(36);
        section.setAlignment(Pos.TOP_CENTER);
        section.setPadding(new Insets(60, 80, 120, 80));
        section.setStyle("-fx-background-color: " + BG + ";");
        section.setMinHeight(600);

        Label header = new Label("Progres");
        setFont(header, "JetBrains Mono", 11, false);
        header.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-opacity: 0.7;");

        int pct = student != null
                ? (int)(new ProgressTracker().getOverallProgress() * 100) : 0;

        List<com.example.javademy.model.Lesson> lessons = dataStorage.loadLessons();
        ProgressTracker pt = student != null ? new ProgressTracker() : null;
        long completed = pt != null ? lessons.stream()
                .filter(l -> pt.getTopicProgress(l.getTopicId()) >= 1.0).count() : 0;

        Label remainingLbl = new Label((lessons.size() - completed) + " lekcji pozostało");
        setFont(remainingLbl, "JetBrains Mono", 11, false);
        remainingLbl.setStyle("-fx-text-fill: " + TEXT2 + ";");

        section.getChildren().addAll(header, buildProgressRing(pct),
                buildProgressBars(), buildStreakDots(), remainingLbl);
        return section;
    }

    private Effect makeTrackEffect() {
        InnerShadow innerDark = new InnerShadow(BlurType.GAUSSIAN,
                Color.rgb(0, 0, 0, 0.15), 10, 0, -3, -3);
        InnerShadow innerLight = new InnerShadow(BlurType.GAUSSIAN,
                Color.rgb(255, 255, 255, 0.80), 8, 0, 3, 3);
        innerLight.setInput(innerDark);

        DropShadow outerDark = new DropShadow(BlurType.GAUSSIAN,
                Color.rgb(180, 150, 120, 0.50), 10, 0, 4, 5);
        outerDark.setInput(innerLight);

        DropShadow outerLight = new DropShadow(BlurType.GAUSSIAN,
                Color.rgb(255, 255, 255, 0.90), 10, 0, -4, -5);
        outerLight.setInput(outerDark);

        return outerLight;
    }

    private StackPane buildProgressRing(int percent) {
        double SIZE  = 220;
        double cx = SIZE / 2.0;
        double cy = SIZE / 2.0;
        double radius = 82;
        double strokeW = 13;
        double angle = (percent / 100.0) * 360;

        Canvas canvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(Color.web("#EDE3D5"));
        gc.setLineWidth(strokeW);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, 0, 360, ArcType.OPEN);

        if (angle > 0) {
            gc.setStroke(Color.web("#DEBDAD"));
            gc.setLineWidth(strokeW);
            gc.setLineCap(StrokeLineCap.ROUND);
            gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, 90, -angle, ArcType.OPEN);
        }

        canvas.setEffect(makeTrackEffect());

        Label pctLabel = new Label(percent + "%");
        setFont(pctLabel, "JetBrains Mono", 32, true);
        pctLabel.setStyle("-fx-text-fill: #7A6A60;");

        Label subLabel = new Label("Ukończono");
        setFont(subLabel, "JetBrains Mono", 12, false);
        subLabel.setStyle("-fx-text-fill: #A89890;");

        VBox labels = new VBox(2, pctLabel, subLabel);
        labels.setAlignment(Pos.CENTER);

        StackPane ring = new StackPane(canvas, labels);
        ring.setAlignment(Pos.CENTER);
        ring.setPrefSize(SIZE, SIZE);
        ring.setMaxSize(SIZE, SIZE);

        return ring;
    }

    private VBox buildProgressBars() {
        VBox bars = new VBox(16);
        bars.setMaxWidth(520);
        bars.setAlignment(Pos.CENTER_LEFT);

        List<Topic> topics = dataStorage.loadTopics();
        ProgressTracker tracker = student != null ? new ProgressTracker() : null;

        for (Topic topic : topics) {
            double progress = tracker != null ? tracker.getTopicProgress(topic.getId()) : 0.0;

            Label lbl = new Label(topic.getName());
            setFont(lbl, "JetBrains Mono", 12, false);
            lbl.setStyle("-fx-text-fill: " + TEXT1 + ";");

            Label pct = new Label((int)(progress * 100) + "%");
            setFont(pct, "JetBrains Mono", 11, false);
            pct.setStyle("-fx-text-fill: " + TEXT2 + ";");

            HBox headerRow = new HBox();
            headerRow.setAlignment(Pos.CENTER_LEFT);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            headerRow.getChildren().addAll(lbl, sp, pct);

            Canvas barCanvas = new Canvas(520, 10);
            drawSoftBar(barCanvas, progress);

            bars.getChildren().add(new VBox(5, headerRow, barCanvas));
        }
        return bars;
    }

    private void drawSoftBar(Canvas canvas, double progress) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight(), r = h / 2.0;

        gc.save();
        gc.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.web("#B8A898", 0.25), 3, 0.2, 0, 1));
        gc.setFill(Color.web("#EAE0D2"));
        gc.fillRoundRect(0, 0, w, h, r*2, r*2);
        gc.restore();

        if (progress > 0.001) {
            double fillW = Math.max(r * 2, w * progress);

            gc.save();
            LinearGradient grad = new LinearGradient(0, 0, fillW, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#E8B8AD")),
                    new Stop(1.0, Color.web("#DDA898")));
            gc.setFill(grad);
            gc.fillRoundRect(0, 0, fillW, h, r*2, r*2);
            gc.restore();

            gc.save();
            LinearGradient hl = new LinearGradient(0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#FFFFFF", 0.30)),
                    new Stop(0.5, Color.web("#FFFFFF", 0.05)),
                    new Stop(1.0, Color.web("#FFFFFF", 0.00)));
            gc.setFill(hl);
            gc.fillRoundRect(0, 0, fillW, h * 0.6, r*2, r*2);
            gc.restore();
        }
    }

    private HBox buildStreakDots() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        int streak = student != null ? student.getLoginStreak() : 0;
        Label lbl = new Label("Seria: " + streak + " dni   ");
        setFont(lbl, "JetBrains Mono", 11, false);
        lbl.setStyle("-fx-text-fill: " + TEXT2 + ";");
        box.getChildren().add(lbl);
        for (int i = 0; i < 7; i++) {
            Circle dot = new Circle(5);
            if (i < streak) dot.setFill(Color.web(LIGHT_RED));
            else {
                dot.setFill(Color.TRANSPARENT);
                dot.setStroke(Color.web(BORDER_BEIGE));
                dot.setStrokeWidth(1.2);
            }
            box.getChildren().add(dot);
        }
        return box;
    }

    private Label navItem(String text) {
        Label l = new Label(text);
        setFont(l, "JetBrains Mono", 13, false);
        l.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
        l.setOnMouseEntered(_ -> l.setStyle(
                "-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand; -fx-underline: true;"));
        l.setOnMouseExited(_ -> {
            setFont(l, "JetBrains Mono", 13, false);
            l.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
        });
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        setFont(l, "JetBrains Mono", 13, false);
        l.setStyle("-fx-text-fill: " + BORDER_BEIGE + "; -fx-padding: 0 2 0 2;");
        return l;
    }

    private VBox buildBottomButtons() {
        Label mojeKonto = bottomBtn("Moje konto");
        mojeKonto.setOnMousePressed(_ -> { if (onNavigate != null) onNavigate.accept("konto"); });
        Label wyloguj = bottomBtn("Wyloguj");
        wyloguj.setOnMousePressed(_ -> { if (onNavigate != null) onNavigate.accept("logout"); });
        VBox box = new VBox(6, mojeKonto, wyloguj);
        box.setPickOnBounds(false);
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return box;
    }

    private Label bottomBtn(String text) {
        Label lbl = new Label(text);
        setFont(lbl, "JetBrains Mono", 13, false);
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand;" +
                "-fx-background-color: " + BG + "; -fx-background-radius: 8;" +
                "-fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 8; -fx-border-width: 1;";
        String hov = "-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;" +
                "-fx-background-color: " + BG + "; -fx-background-radius: 8;" +
                "-fx-border-color: " + TEXT2 + "; -fx-border-radius: 8; -fx-border-width: 1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited(_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }

    /**
     * Zwraca widok dashboardu z przyciskiem wylogowania.
     *
     * @return panel główny ProgressDashboard
     */
    public StackPane getView() {
        VBox bottonBtnGroup = buildBottomButtons();
        Rectangle fadeOverlay = new Rectangle();
        fadeOverlay.setMouseTransparent(true);
        fadeOverlay.widthProperty().bind(scroll.widthProperty());
        fadeOverlay.heightProperty().bind(scroll.heightProperty());
        fadeOverlay.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.78, Color.TRANSPARENT),
                new Stop(1.0, Color.web(BG))));
        StackPane wrapper = new StackPane(root, fadeOverlay, bottonBtnGroup);
        StackPane.setAlignment(fadeOverlay, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(bottonBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottonBtnGroup, new Insets(0, 0, 20, 20));

        VBox repairWidget = buildRepairWidget();
        if (repairWidget != null) {
            VBox widgetAnchor = new VBox(repairWidget);
            widgetAnchor.setAlignment(Pos.BOTTOM_RIGHT);
            widgetAnchor.setPickOnBounds(false);
            wrapper.getChildren().add(widgetAnchor);
            StackPane.setAlignment(widgetAnchor, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(widgetAnchor, new Insets(0, 20, 20, 0));
        }

        return wrapper;
    }

    private VBox buildRepairWidget() {
        if (student == null) return null;
        java.util.Set<String> weakIds = student.getWeakExerciseIds();
        if (weakIds.isEmpty()) return null;

        java.util.List<com.example.javademy.model.Exercise> allEx = dataStorage.loadExercises();
        long quizCount = allEx.stream().filter(e -> weakIds.contains(e.getId()) && e instanceof com.example.javademy.model.QuizExercise).count();
        long codeCount = allEx.stream().filter(e -> weakIds.contains(e.getId()) && e instanceof com.example.javademy.model.CodeExercise).count();

        String countText;
        if (quizCount > 0 && codeCount > 0)
            countText = quizCount + " " + (quizCount == 1 ? "pytanie" : quizCount < 5 ? "pytania" : "pytań")
                    + "  ·  " + codeCount + " " + (codeCount == 1 ? "zadanie" : codeCount < 5 ? "zadania" : "zadań");
        else if (quizCount > 0)
            countText = quizCount + " " + (quizCount == 1 ? "pytanie" : quizCount < 5 ? "pytania" : "pytań") + " do powtórki";
        else
            countText = codeCount + " " + (codeCount == 1 ? "zadanie" : codeCount < 5 ? "zadania" : "zadań") + " do poprawy";

        Label title = new Label("Szlifuj wiedzę");
        setFont(title, "JetBrains Mono", 12, true);
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Label counts = new Label(countText);
        setFont(counts, "JetBrains Mono", 11, false);
        counts.setStyle("-fx-text-fill: " + TEXT2 + ";");

        Button btn = new Button("Ogarnij to!");
        btn.setPrefSize(150, 38);
        btn.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, 12));
        btn.setStyle("-fx-background-color: #B6866A; -fx-background-radius: 19; -fx-text-fill: white; -fx-cursor: hand;");
        InnerShadow bhi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 8, 0, -2, -2);
        InnerShadow bsh = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15), 6, 0, 2, 2);
        bsh.setInput(bhi);
        DropShadow bdrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12), 8, 0, 0, 4);
        bdrop.setInput(bsh);
        btn.setEffect(bdrop);
        btn.setOnMouseEntered(_ -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), btn); st.setToX(1.04); st.setToY(1.04); st.play(); bdrop.setRadius(12); bdrop.setOffsetY(6); });
        btn.setOnMouseExited (_ -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), btn); st.setToX(1.0);  st.setToY(1.0);  st.play(); bdrop.setRadius(8);  bdrop.setOffsetY(4); });
        btn.setOnAction(_ -> { if (onNavigate != null) onNavigate.accept("zaleglosci"); });

        HBox btnRow = new HBox(btn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        VBox card = new VBox(5, title, counts, btnRow);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setMaxWidth(230);
        card.setStyle("-fx-background-color: rgba(245,238,229,0.65); -fx-background-radius: 18; -fx-border-color: rgba(255,255,255,0.55); -fx-border-width: 1; -fx-border-radius: 18;");
        DropShadow cardShadow = new DropShadow(BlurType.GAUSSIAN, Color.web("#9B7A6A", 0.22), 16, 0, 0, 6);
        InnerShadow cardHi = new InnerShadow(BlurType.GAUSSIAN, Color.web("#FFFFFF", 0.50), 10, 0, -1, -1);
        cardHi.setInput(cardShadow);
        card.setEffect(cardHi);

        return card;
    }
}