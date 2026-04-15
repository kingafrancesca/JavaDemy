package com.example.javademy.ui;

import com.example.javademy.ai.ProgressTracker;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.engine.CodeGuard;
import com.example.javademy.engine.CodeRunner;
import com.example.javademy.model.*;
import com.example.javademy.storage.DataStorage;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widok lekcji w aplikacji JavaDemy.
 * Obsługuje trzy ekrany: listę lekcji, treść aktywnej lekcji z rozdziałami
 * oraz ekran podsumowania po ukończeniu lekcji.
 */
public class LessonView
{
    private final DataStorage dataStorage;
    private final StackPane view;
    private int currentStep;
    private Lesson currentLesson;
    private boolean fromRepairPlan = false;
    private Consumer<String> onNavigate;

    private static final String BG = "#F5ECE1";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BORDER_BEIGE = "#DDD0C0";
    private static final String SAGE = "#9FB395";
    private static final String TERRA = "#B6866A";
    private static final String GREY = "#BABABA";
    private static final String CODE_BG = "#EFE4D1";
    private static final String CREAM = "#E8E1D5";

    /** Tworzy LessonView. */
    public LessonView() {
        this(null);
    }

    /**
     * Tworzy LessonView z argumentem zwracającym wywoływanym po ukończeniu lekcji.
     *
     * @param cb argument wywoływany po zakończeniu lekcji
     */
    public LessonView(Runnable cb) {
        this(cb, null);
    }

    /**
     * Tworzy LessonView z akcją ukończenia lekcji i akcją nawigacji.
     *
     * @param ignored akcja wywoływana po zakończeniu lekcji (nieużywana, zarezerwowana)
     * @param nav     akcja nawigacji przyjmująca nazwę widoku docelowego
     */
    public LessonView(Runnable ignored, Consumer<String> nav)
    {
        this.dataStorage = new DataStorage();
        this.currentStep = 0;
        this.onNavigate = nav;
        this.view = new StackPane();
        loadFonts();
        showLessonList();
    }

    /**
     * Ustawia callback nawigacji do innych widoków aplikacji.
     *
     * @param nav funkcja przyjmująca nazwę docelowego widoku
     */
    public void setOnNavigate(Consumer<String> nav) { this.onNavigate = nav; }

    /**
     * Otwiera bezpośrednio lekcję o podanym id od początku (w przypadku RepairPlanView).
     *
     * @param lessonId identyfikator lekcji do otwarcia.
     */
    public void openLessonById(String lessonId) {
        this.fromRepairPlan = true;
        dataStorage.loadLessons().stream().filter(l -> lessonId.equals(l.getId())).findFirst().ifPresent(lesson -> {
                this.currentLesson = lesson;
                this.currentStep   = 0;
                showCurrentStep();
            });
    }

    private void setFont(Label l, double size) {
        l.setFont(Font.font("JetBrains Mono", FontWeight.NORMAL, size));
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

    private HBox buildNavBar(String active)
    {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(14, 28, 14, 28));
        nav.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");

        Region sL  = new Region(); HBox.setHgrow(sL,  Priority.ALWAYS);
        Region sM1 = new Region(); HBox.setHgrow(sM1, Priority.ALWAYS);
        Region sM2 = new Region(); HBox.setHgrow(sM2, Priority.ALWAYS);
        Region sR  = new Region(); HBox.setHgrow(sR,  Priority.ALWAYS);

        Label lekcje = navItem("Lekcje", "lekcje".equals(active));
        Label sep1 = navSep();
        Label codelab = navItem("Code Lab", "codelab".equals(active));
        lekcje .setOnMouseClicked(_ -> navigate("lekcje"));
        codelab.setOnMouseClicked(_ -> navigate("codelab"));

        Label logo = new Label("JavaDemy");
        logo.setFont(Font.font("Playfair Display", 22));
        logo.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        logo.setOnMouseClicked(_ -> navigate("dashboard"));

        Label zadania = navItem("Zadania", "zadania".equals(active));
        Label sep2 = navSep();
        Label quizy = navItem("Quizy", "quizy".equals(active));
        zadania.setOnMouseClicked(_ -> navigate("zadania"));
        quizy.setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private void navigate(String dest) {
        if (onNavigate != null) onNavigate.accept(dest);
        else if ("lekcje".equals(dest)) showLessonList();
    }

    private Label navItem(String text, boolean active)
    {
        Label l = new Label(text);
        l.setFont(Font.font("JetBrains Mono", 13));
        if (active) l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand; -fx-border-color: transparent transparent " + SAGE + " transparent; -fx-border-width: 0 0 2 0;");
        else {
            l.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
            l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
            l.setOnMouseExited (_ -> l.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        }
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        l.setFont(Font.font("JetBrains Mono", 13));
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
        setFont(lbl, 13);
        lbl.setPadding(new Insets(10, 16, 10, 16));
        String def = "-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 8; -fx-border-width: 1;";
        String hov = "-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + TEXT2 + "; -fx-border-radius: 8; -fx-border-width: 1;";
        lbl.setStyle(def);
        lbl.setOnMouseEntered(_ -> lbl.setStyle(hov));
        lbl.setOnMouseExited(_ -> lbl.setStyle(def));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(javafx.geometry.Pos.CENTER);
        return lbl;
    }

    private void showLessonList()
    {
        VBox content = new VBox(7);
        content.setPadding(new Insets(8, 20, 60, 20));

        Label pageTitle = new Label("Lekcje");
        setFont(pageTitle, 16);
        pageTitle.setStyle("-fx-text-fill: " + TEXT2 + ";");

        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendDot(SAGE,  "Ukończono"),
                legendDot(TERRA, "W toku"),
                legendDot(GREY,  "Nie rozpoczęto")
        );

        VBox stickyHeader = new VBox(8, pageTitle, legend);
        stickyHeader.setAlignment(Pos.CENTER);
        stickyHeader.setPadding(new Insets(20, 0, 12, 0));
        stickyHeader.setMaxWidth(820);
        stickyHeader.setStyle("-fx-background-color: " + BG + ";");

        List<Topic> topics = dataStorage.loadTopics();
        List<Lesson> lessons = dataStorage.loadLessons();
        Student student = getStudent();

        for (Topic topic : topics) {
            List<Lesson> topicLessons = lessons.stream().filter(l -> l.getTopicId().equals(topic.getId())).toList();

            double totalProg = 0;
            for (Lesson l : topicLessons) {
                List<String> sIds = ProgressTracker.getSectionIds(l);
                totalProg += student != null ? student.getLessonProgress(sIds) : 0.0;
            }
            double moduleProg = topicLessons.isEmpty() ? 0 : totalProg / topicLessons.size();
            String pillColor = moduleProg >= 1.0 ? SAGE : (moduleProg > 0 ? TERRA : GREY);

            HBox mh = new HBox(10);
            mh.setAlignment(Pos.CENTER_LEFT);
            mh.setPadding(new Insets(20, 0, 0, 0));

            Label pill = new Label(topic.getName().split(" ")[0]);
            setFont(pill, 11);
            pill.setStyle("-fx-background-color: " + pillColor + "; -fx-background-radius: 20; -fx-padding: 3 12; -fx-text-fill: white;");

            mh.getChildren().addAll(pill);
            content.getChildren().add(mh);

            for (Lesson lesson : topicLessons) {
                List<String> sIds = ProgressTracker.getSectionIds(lesson);
                double prog = student != null ? student.getLessonProgress(sIds) : 0.0;
                int percent  = (int)(prog * 100);
                String barColor = percent == 100 ? SAGE : (percent > 0 ? TERRA : GREY);
                content.getChildren().add(buildLessonRow(lesson, prog, percent, barColor));
            }
        }

        content.setMaxWidth(820);
        VBox inner = new VBox(content);
        inner.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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

        StackPane headerWrapper = new StackPane(stickyHeader);
        StackPane.setAlignment(stickyHeader, Pos.TOP_CENTER);
        headerWrapper.setPadding(new Insets(0, 40, 0, 40));
        headerWrapper.setStyle("-fx-background-color: " + BG + ";");

        VBox root = new VBox(0, buildNavBar("lekcje"), headerWrapper, scrollWithFade);
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox bottomBtnGroup = buildBottomButtons();
        StackPane wrapper = new StackPane(root, bottomBtnGroup);
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));

        view.getChildren().setAll(wrapper);
    }

    private HBox buildLessonRow(Lesson lesson, double prog, int percent, String barColor)
    {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(18, 25, 18, 25));

        row.setStyle("-fx-background-color: rgba(252,248,243,0.72); -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.60); -fx-border-radius: 25; -fx-border-width: 1;");

        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 14, 0, -2, -2);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.06),        8, 0,  1,  1);
        lo.setInput(hi);
        row.setEffect(lo);

        Label name = new Label(lesson.getTitle());
        setFont(name, 13);
        name.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);

        Rectangle trackR = new Rectangle(200, 5);
        trackR.setArcWidth(5); trackR.setArcHeight(5);
        trackR.setFill(Color.web(CREAM));

        double fillW = prog > 0.001 ? Math.max(8, 200 * prog) : 0;
        Rectangle fillR = new Rectangle(fillW, 5);
        fillR.setArcWidth(5); fillR.setArcHeight(5);
        fillR.setFill(Color.web(barColor));

        StackPane barStack = new StackPane(trackR, fillR);
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.setPrefWidth(200);

        Label percentLabel = new Label(percent + "%");
        setFont(percentLabel, 11);
        percentLabel.setStyle("-fx-text-fill: " + TEXT2 + ";");
        percentLabel.setPrefWidth(38);
        percentLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(name, rowSpacer, barStack, percentLabel);
        row.setOnMouseClicked(_ -> openLesson(lesson));
        row.setOnMouseEntered(_ -> row.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.80); -fx-border-radius: 20; -fx-border-width: 1;"));
        row.setOnMouseExited(_ -> row.setStyle("-fx-background-color: rgba(252,248,243,0.72); -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.60); -fx-border-radius: 20; -fx-border-width: 1;"));
        return row;
    }

    private HBox legendDot(String color, String text) {
        HBox item = new HBox(6); item.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(color));
        Label lbl  = new Label(text);
        setFont(lbl, 11);
        lbl.setStyle("-fx-text-fill: " + TEXT2 + ";");
        item.getChildren().addAll(dot, lbl);
        return item;
    }

    private void openLesson(Lesson lesson) {
        this.currentLesson = lesson;
        this.currentStep = findFirstUncompletedStep(lesson);
        showCurrentStep();
    }

    private int findFirstUncompletedStep(Lesson lesson) {
        Student s = getStudent();
        if (s == null) return 0;
        List<Lesson.LessonStep> steps = lesson.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            String sid = steps.get(i).getSectionId();
            if (sid == null || !s.isStepCompleted(sid)) return i;
        }
        return steps.size() - 1;
    }

    private void showCurrentStep()
    {
        VBox card = buildLessonCard();

        StackPane cardWrapper = new StackPane(card);
        cardWrapper.setPadding(new Insets(10, 60, 10, 60));
        cardWrapper.setStyle("-fx-background-color: " + BG + ";");

        DropShadow wrapperShadow = new DropShadow();
        wrapperShadow.setRadius(20); wrapperShadow.setOffsetY(6);
        wrapperShadow.setColor(Color.rgb(160, 135, 110, 0.18));
        cardWrapper.setEffect(wrapperShadow);

        cardWrapper.setMaxWidth(1100);
        StackPane centeredWrapper = new StackPane(cardWrapper);
        centeredWrapper.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(centeredWrapper);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(0, buildNavBar(fromRepairPlan ? "" : "lekcje"), buildSubNav(), scroll);
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox bottomBtnGroup = buildBottomButtons();
        StackPane wrapper = new StackPane(root, bottomBtnGroup);
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));

        view.getChildren().setAll(wrapper);
    }

    private HBox buildSubNav() {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(10, 28, 10, 28));
        nav.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent transparent " + BORDER_BEIGE + " transparent; -fx-border-width: 0 0 1 0;");

        Button back = new Button(fromRepairPlan ? "←" : "← Lista lekcji");
        back.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT2 + "; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-cursor: hand;");
        back.setOnAction(_ -> {
            if (fromRepairPlan) {
                fromRepairPlan = false; navigate("zaleglosci");
            }
            else showLessonList();
        });

        Region sL = new Region(); HBox.setHgrow(sL, Priority.ALWAYS);
        Label title = new Label(currentLesson != null ? currentLesson.getTitle() : "");
        setFont(title, 13);
        title.setStyle("-fx-text-fill: " + TEXT2 + ";");
        Region sR = new Region(); HBox.setHgrow(sR, Priority.ALWAYS);

        Label stepLbl = new Label(currentLesson != null ? "Krok " + (currentStep+1) + " z " + currentLesson.getStepCount() : "");
        setFont(stepLbl, 12);
        stepLbl.setStyle("-fx-text-fill: " + TEXT2 + ";");

        nav.getChildren().addAll(back, sL, title, sR, stepLbl);
        return nav;
    }

    private VBox buildLessonCard()
    {
        VBox card = new VBox(20);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle("-fx-background-color: rgba(255, 252, 248, 0.78); -fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.70); -fx-border-width: 1.5; -fx-border-radius: 24;");

        InnerShadow hi = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 14, 0, -2, -2);
        InnerShadow lo = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.06),        8, 0,  1,  1);
        lo.setInput(hi);
        card.setEffect(lo);

        if (currentLesson == null || currentLesson.getSteps().isEmpty()) return card;
        Lesson.LessonStep step = currentLesson.getSteps().get(currentStep);

        Label stepTitle = new Label(step.getTitle());
        stepTitle.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 24));
        stepTitle.setStyle("-fx-text-fill: " + TEXT1 + ";");
        stepTitle.setWrapText(true);
        stepTitle.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(stepTitle);

        if (currentLesson.getStepCount() > 1) card.getChildren().add(buildThinProgressBar((double)(currentStep+1) / currentLesson.getStepCount()));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER_BEIGE + "; -fx-opacity: 0.5;");
        card.getChildren().add(sep);

        buildFormattedContent(step.getContent(), card);

        List<Lesson.CodeExample> examples = step.getCodeExamples();
        if (examples != null)
            for (Lesson.CodeExample ex : examples)
                card.getChildren().add(buildCodeBlock(ex));

        card.getChildren().add(buildArrowNavigation());
        return card;
    }

    private HBox buildThinProgressBar(double progress) {
        Rectangle track = new Rectangle(1, 4);
        track.setFill(Color.web(CREAM));
        track.setArcWidth(4); track.setArcHeight(4);

        Rectangle fill = new Rectangle(1, 4);
        fill.setFill(Color.web(SAGE));
        fill.setArcWidth(4); fill.setArcHeight(4);

        StackPane bar = new StackPane(track, fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setMaxWidth(Double.MAX_VALUE);

        bar.widthProperty().addListener((_, _, w) -> {
            track.setWidth(w.doubleValue());
            fill.setWidth(w.doubleValue() * progress);
        });

        HBox wrapper = new HBox(bar);
        HBox.setHgrow(bar, Priority.ALWAYS);
        return wrapper;
    }

    private HBox buildArrowNavigation() {
        HBox nav = new HBox();
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(16, 0, 0, 0));

        Button prev = buildArrowButton("←");
        prev.setDisable(currentStep == 0);
        prev.setOnAction(_ -> { currentStep--; showCurrentStep(); });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button next;
        if (currentStep == currentLesson.getStepCount() - 1) {
            next = buildArrowButton("✓");
            next.setOnAction(_ -> { markCurrentStepCompleted(); showLessonFinishedScreen(); });
        }
        else {
            next = buildArrowButton("→");
            next.setOnAction(_ -> { markCurrentStepCompleted(); currentStep++; showCurrentStep(); });
        }

        nav.getChildren().addAll(prev, spacer, next);
        return nav;
    }

    private Button buildArrowButton(String symbol) {
        Button btn = new Button(symbol);
        btn.setPrefSize(48, 48);
        btn.setFont(Font.font("JetBrains Mono", 18));
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.55); -fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 24; -fx-border-width: 1; -fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        InnerShadow d = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.10), 6, 0, 2, 2);
        InnerShadow h = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.65), 5, 0, -2, -2);
        h.setInput(d);
        DropShadow s = new DropShadow(); s.setRadius(8); s.setOffsetY(3);
        s.setColor(Color.rgb(0,0,0,0.08)); s.setInput(h);
        btn.setEffect(s);

        btn.setOnMouseEntered(_ -> { ScaleTransition t = new ScaleTransition(Duration.millis(150), btn); t.setToX(1.08); t.setToY(1.08); t.play(); });
        btn.setOnMouseExited (_ -> { ScaleTransition t = new ScaleTransition(Duration.millis(150), btn); t.setToX(1.0);  t.setToY(1.0);  t.play(); });
        return btn;
    }

    private void showLessonFinishedScreen()
    {
        VBox body = new VBox(10);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(16, 80, 16, 80));
        body.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(body, Priority.ALWAYS);

        Student student = getStudent();
        List<String> sIds = ProgressTracker.getSectionIds(currentLesson);
        double progress = student != null ? student.getLessonProgress(sIds) : 1.0;
        int percent = (int)(progress * 100);

        Label lessonName = new Label(currentLesson.getTitle());
        setFont(lessonName, 12);
        lessonName.setPadding(new Insets(0,0,8,0));
        lessonName.setStyle("-fx-text-fill: " + TEXT2 + ";");

        ImageView badgeImg = new ImageView();
        var imgUrl = getClass().getResource("/images/approved_lesson.png");
        if (imgUrl != null) badgeImg.setImage(new Image(imgUrl.toExternalForm()));
        badgeImg.setFitWidth(220);
        badgeImg.setPreserveRatio(true);
        badgeImg.setSmooth(true);
        VBox.setMargin(badgeImg, new Insets(8, 0, 20, 0));

        Label title = new Label("Lekcja zakończona!");
        title.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: " + TEXT1 + ";");

        Label progLbl = new Label("Postęp lekcji: " + percent + "%");
        setFont(progLbl, 13);
        progLbl.setStyle("-fx-text-fill: " + SAGE + ";");

        StackPane progressBar = buildFinishedProgressBar(progress);
        progressBar.setMaxWidth(380);

        if (fromRepairPlan) {
            Button endBtn = buildPillButton("Zakończ", "primary");
            endBtn.setMaxWidth(380);
            endBtn.setOnAction(_ -> { fromRepairPlan = false; navigate("zaleglosci"); });
            body.getChildren().addAll(lessonName, badgeImg, title, progLbl, progressBar, endBtn);
        }
        else {
            Button quizBtn = buildPillButton("Quiz z tej lekcji","primary");
            Button codeBtn = buildPillButton("Zadania praktyczne","secondary");
            Button backBtn = buildPillButton("Wróć do listy lekcji","tertiary");
            quizBtn.setMaxWidth(380); codeBtn.setMaxWidth(380); backBtn.setMaxWidth(380);
            quizBtn.setOnAction(_ -> showQuizForLesson());
            codeBtn.setOnAction(_ -> showCodeExercisesForLesson());
            backBtn.setOnAction(_ -> showLessonList());
            body.getChildren().addAll(lessonName, badgeImg, title, progLbl, progressBar, quizBtn, codeBtn, backBtn, buildNextLessonHint());
        }

        VBox root = new VBox(0, buildNavBar("lekcje"), body);
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox bottomBtnGroup = buildBottomButtons();
        StackPane wrapper = new StackPane(root, bottomBtnGroup);
        StackPane.setAlignment(bottomBtnGroup, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bottomBtnGroup, new Insets(0, 0, 20, 20));

        view.getChildren().setAll(wrapper);
    }

    private StackPane buildFinishedProgressBar(double progress) {
        Rectangle track = new Rectangle(380, 4);
        track.setArcWidth(4); track.setArcHeight(4);
        track.setFill(Color.web(CREAM));

        Rectangle fill = new Rectangle(380 * progress, 4);
        fill.setArcWidth(4); fill.setArcHeight(4);
        fill.setFill(Color.web(SAGE));

        StackPane bar = new StackPane(track, fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.widthProperty().addListener((_, _, w) -> {
            track.setWidth(w.doubleValue());
            fill.setWidth(w.doubleValue() * progress);
        });
        return bar;
    }

    private Button buildPillButton(String text, String tier) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("JetBrains Mono", 13));
        String style;
        if ("primary".equals(tier))
            style = "-fx-background-color: " + SAGE + "; -fx-background-radius: 25; -fx-text-fill: white; -fx-padding: 11 40; -fx-cursor: hand;";
        else if ("secondary".equals(tier))
            style = "-fx-background-color: transparent; -fx-background-radius: 25; -fx-border-color: " + TERRA + "; -fx-border-radius: 25; -fx-border-width: 1.5; -fx-text-fill: " + TERRA + "; -fx-padding: 10 40; -fx-cursor: hand;";
        else
            style = "-fx-background-color: transparent; -fx-background-radius: 25; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-radius: 25; -fx-border-width: 1; -fx-text-fill: " + TEXT2 + "; -fx-padding: 10 40; -fx-cursor: hand;";
        btn.setStyle(style);
        btn.setOnMouseEntered(_ -> btn.setOpacity(0.85));
        btn.setOnMouseExited (_ -> btn.setOpacity(1.0));
        return btn;
    }

    private Label buildNextLessonHint() {
        List<Lesson> all = dataStorage.loadLessons();
        Lesson next = null; boolean found = false;
        for (Lesson l : all) {
            if (found) { next = l; break; }
            if (l.getId().equals(currentLesson.getId())) found = true;
        }
        String txt = next != null ? "Następna lekcja: " + next.getTitle() + " →" : "To była ostatnia lekcja!";
        Label lbl = new Label(txt);
        setFont(lbl, 12);
        lbl.setPadding(new Insets(4, 0, 0, 0));
        if (next != null) {
            final Lesson nx = next;
            lbl.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand;");
            lbl.setOnMouseClicked(_ -> openLesson(nx));
            lbl.setOnMouseEntered(_ -> lbl.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand; -fx-underline: true;"));
            lbl.setOnMouseExited (_ -> lbl.setStyle("-fx-text-fill: " + TEXT2 + "; -fx-cursor: hand;"));
        }
        else lbl.setStyle("-fx-text-fill: " + TEXT2 + ";");
        return lbl;
    }

    private VBox buildCodeBlock(Lesson.CodeExample ex)
    {
        String lang = (ex.description() != null && !ex.description().isBlank())
                ? ex.description() : "Java";
        Label langLabel = new Label(lang);
        setFont(langLabel, 12);
        langLabel.setStyle("-fx-text-fill: " + TEXT2 + ";");

        Button playBtn = new Button("▶");
        playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TERRA + "; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-cursor: hand;");

        Region hs = new Region(); HBox.setHgrow(hs, Priority.ALWAYS);
        HBox header = new HBox(langLabel, hs, playBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 6, 0));

        Label codeLabel = new Label(ex.code());
        codeLabel.setFont(Font.font("JetBrains Mono", 13));
        codeLabel.setStyle("-fx-text-fill: " + TEXT1 + ";");
        codeLabel.setWrapText(true);
        codeLabel.setMaxWidth(Double.MAX_VALUE);

        Label outputLabel = new Label();
        outputLabel.setFont(Font.font("JetBrains Mono", 12));
        outputLabel.setWrapText(true);
        outputLabel.setMaxWidth(Double.MAX_VALUE);
        outputLabel.setVisible(false);
        outputLabel.setManaged(false);

        Separator outSep = new Separator();
        outSep.setVisible(false);
        outSep.setManaged(false);

        playBtn.setOnAction(_ -> {
            playBtn.setDisable(true);
            playBtn.setText("...");
            new Thread(() -> {
                try {
                    CodeGuard.ExecutionResult result = new CodeRunner().compileAndRun(ex.code());
                    Platform.runLater(() -> {
                        outSep.setVisible(true);     outSep.setManaged(true);
                        outputLabel.setVisible(true); outputLabel.setManaged(true);
                        if (result.success()) {
                            String out = result.output();
                            outputLabel.setText(out.isBlank() ? "(brak wyjścia)" : out);
                            outputLabel.setStyle("-fx-text-fill: " + SAGE + ";");
                        }
                        else {
                            outputLabel.setText(result.errorMessage());
                            outputLabel.setStyle("-fx-text-fill: #C0504D;");
                        }
                        playBtn.setDisable(false); playBtn.setText("▶");
                    });
                } catch (Exception ex2) {
                    Platform.runLater(() -> {
                        outputLabel.setVisible(true); outputLabel.setManaged(true);
                        outputLabel.setText(ex2.getMessage());
                        outputLabel.setStyle("-fx-text-fill: #C0504D;");
                        playBtn.setDisable(false); playBtn.setText("▶");
                    });
                }
            }).start();
        });

        VBox innerBlock = new VBox(10, codeLabel, outSep, outputLabel);
        innerBlock.setPadding(new Insets(14, 16, 14, 16));
        innerBlock.setMaxWidth(Double.MAX_VALUE);
        innerBlock.setStyle("-fx-background-color: #EAE0D0; -fx-background-radius: 12;");
        InnerShadow sunkenDark  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.09),       7, 0,  2,  2);
        InnerShadow sunkenLight = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 5, 0, -1, -1);
        sunkenLight.setInput(sunkenDark);
        innerBlock.setEffect(sunkenLight);

        VBox block = new VBox(10, header, innerBlock);
        block.setPadding(new Insets(14));
        block.setMaxWidth(Double.MAX_VALUE);
        block.setStyle("-fx-background-color: rgba(245,238,228,0.55); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.45); -fx-border-radius: 16; -fx-border-width: 1;");
        InnerShadow outerHL = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.30), 12, 0, -1, -1);
        InnerShadow outerDepth = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.04),        8, 0,  1,  1);
        outerDepth.setInput(outerHL);
        block.setEffect(outerDepth);

        return block;
    }

    private void buildFormattedContent(String raw, VBox target)
    {
        if (raw == null || raw.isBlank()) return;
        String[] lines = raw.split("\n");
        int idx = 0;
        while (idx < lines.length) {
            String t = lines[idx].trim();
            if (t.startsWith("|")) {
                List<String> tableRows = new ArrayList<>();
                while (idx < lines.length && lines[idx].trim().startsWith("|")) {
                    tableRows.add(lines[idx].trim());
                    idx++;
                }
                target.getChildren().add(buildTableNode(tableRows));
                continue;
            }
            idx++;
            if (t.isEmpty()) {
                Region sp = new Region(); sp.setPrefHeight(6); target.getChildren().add(sp);
                continue;
            }
            if (t.startsWith("> ")) {
                Label code = new Label(t.substring(2));
                code.setFont(Font.font("JetBrains Mono", 13));
                code.setStyle("-fx-text-fill: " + TERRA + "; -fx-padding: 6 0 6 0;");
                code.setTextAlignment(TextAlignment.CENTER);
                code.setAlignment(Pos.CENTER);
                code.setWrapText(true);
                code.setMaxWidth(Double.MAX_VALUE);
                target.getChildren().add(code);
            }
            else if (t.startsWith("### ")) {
                Label h = new Label(t.substring(4));
                h.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 14));
                h.setStyle("-fx-text-fill: " + TERRA + "; -fx-padding: 6 0 1 0;");
                h.setWrapText(true);
                h.setMaxWidth(Double.MAX_VALUE);
                target.getChildren().add(h);
            }
            else if (t.startsWith("## ")) {
                Label h = new Label(t.substring(3));
                h.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 15));
                h.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 8 0 2 0;");
                h.setWrapText(true);
                h.setMaxWidth(Double.MAX_VALUE);
                target.getChildren().add(h);
            }
            else if (t.matches("\\d+\\.\\s+.*")) {
                HBox item = new HBox(10); item.setAlignment(Pos.TOP_LEFT);
                int dotIdx = t.indexOf('.');
                Label num = new Label(t.substring(0, dotIdx + 1));
                num.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 14));
                num.setStyle("-fx-text-fill: " + TERRA + ";");
                num.setMinWidth(28);
                String numContent = t.substring(dotIdx + 1).trim();
                javafx.scene.Node numNode = buildInlineNode(numContent, 14);
                HBox.setHgrow(numNode, Priority.ALWAYS);
                item.getChildren().addAll(num, numNode);
                target.getChildren().add(item);
            }
            else if (t.startsWith("- ") || t.startsWith("• ")) {
                HBox bullet = new HBox(10); bullet.setAlignment(Pos.TOP_LEFT);
                Label dash = new Label("-");
                dash.setFont(Font.font("JetBrains Mono", 14));
                dash.setStyle("-fx-text-fill: " + TEXT2 + ";");
                dash.setMinWidth(20);
                String content = t.replaceFirst("^[-•]\\s*", "");
                javafx.scene.Node contentNode = buildInlineNode(content, 14);
                HBox.setHgrow(contentNode, Priority.ALWAYS);
                bullet.getChildren().addAll(dash, contentNode);
                target.getChildren().add(bullet);
            }
            else {
                javafx.scene.Node para = buildInlineNode(t, 15);
                if (para instanceof TextFlow tf) tf.setLineSpacing(4);
                else if (para instanceof Label l) l.setLineSpacing(4);
                target.getChildren().add(para);
            }
        }
    }

    private javafx.scene.Node buildTableNode(List<String> rows)
    {
        int numCols = 0;
        for (String row : rows) {
            if (row.replaceAll("[|\\-\\s]", "").isEmpty()) continue;
            String[] cells = row.split("\\|", -1);
            numCols = Math.max(numCols, cells.length - 2);
            break;
        }
        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(8, 0, 8, 0));
        for (int c = 0; c < numCols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / numCols);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }
        int rowIdx = 0;
        for (String row : rows) {
            if (row.replaceAll("[|\\-\\s]", "").isEmpty()) continue; // separator
            String[] cells = row.split("\\|", -1);
            boolean isHeader = (rowIdx == 0);
            int colIdx = 0;
            for (int c = 1; c < cells.length - 1; c++) {
                String cell = cells[c].trim();
                Label lbl = new Label(cell);
                lbl.setFont(Font.font("JetBrains Mono",
                        isHeader ? FontWeight.BOLD : FontWeight.NORMAL, 13));
                lbl.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 6 12 6 12; -fx-border-color: " + BORDER_BEIGE + "; -fx-border-width: 1; -fx-background-color: " + (isHeader ? CODE_BG : "transparent") + ";");
                lbl.setWrapText(true);
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setMaxHeight(Double.MAX_VALUE);
                GridPane.setFillHeight(lbl, true);
                GridPane.setValignment(lbl, VPos.CENTER);
                grid.add(lbl, colIdx++, rowIdx);
            }
            rowIdx++;
        }
        return grid;
    }

    private javafx.scene.Node buildInlineNode(String text, double fontSize)
    {
        if (!text.contains("**") && !text.contains("`") && !text.contains("*")) {
            Label l = new Label(text);
            l.setFont(Font.font("JetBrains Mono", fontSize));
            l.setStyle("-fx-text-fill: " + TEXT1 + ";");
            l.setWrapText(true);
            l.setMaxWidth(Double.MAX_VALUE);
            return l;
        }
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setLineSpacing(3);

        int i = 0;
        StringBuilder plain = new StringBuilder();

        while (i < text.length()) {
            char c = text.charAt(i);

            if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                flushPlain(plain, flow, fontSize);
                int end = text.indexOf("**", i + 2);
                if (end < 0) { plain.append("**"); i += 2; continue; }
                Text bold = new Text(text.substring(i + 2, end));
                bold.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, fontSize));
                bold.setFill(javafx.scene.paint.Color.web(TEXT1));
                flow.getChildren().add(bold);
                i = end + 2;

            }
            else if (c == '`') {
                flushPlain(plain, flow, fontSize);
                int end = text.indexOf('`', i + 1);
                if (end < 0) { plain.append('`'); i++; continue; }
                Text code = new Text(text.substring(i + 1, end));
                code.setFont(Font.font("JetBrains Mono", fontSize - 1));
                code.setFill(javafx.scene.paint.Color.web(TERRA));
                flow.getChildren().add(code);
                i = end + 1;

            }
            else if (c == '*') {
                flushPlain(plain, flow, fontSize);
                int end = text.indexOf('*', i + 1);
                if (end < 0) { plain.append('*'); i++; continue; }
                Text italic = new Text(text.substring(i + 1, end));
                italic.setFont(Font.font("JetBrains Mono", FontPosture.ITALIC, fontSize));
                italic.setFill(javafx.scene.paint.Color.web(TEXT2));
                flow.getChildren().add(italic);
                i = end + 1;

            }
            else {
                plain.append(c);
                i++;
            }
        }
        flushPlain(plain, flow, fontSize);
        return flow;
    }

    private void flushPlain(StringBuilder sb, TextFlow flow, double fontSize)
    {
        if (sb.isEmpty()) return;
        Text t = new Text(sb.toString());
        t.setFont(Font.font("JetBrains Mono", fontSize));
        t.setFill(javafx.scene.paint.Color.web(TEXT1));
        flow.getChildren().add(t);
        sb.setLength(0);
    }

    private void markCurrentStepCompleted()
    {
        if (currentLesson == null) return;
        Lesson.LessonStep step = currentLesson.getSteps().get(currentStep);
        String sid = step.getSectionId();
        if (sid == null || sid.isBlank()) return;
        Student s = getStudent();
        if (s == null) return;
        s.completeStep(sid);
        try {
            new com.example.javademy.auth.ProfileManager().saveStudent(s);
        }
        catch (Exception ex) {
            System.err.println("Błąd zapisu postępu: " + ex.getMessage());
        }
    }

    private void showQuizForLesson() {
        if (currentLesson == null) return;
        List<QuizExercise> q = dataStorage.loadQuizExercisesForLesson(currentLesson.getId());
        QuizView qv = new QuizView();
        qv.setOnNavigate(onNavigate);
        qv.startQuizForLesson(currentLesson, q);
        view.getChildren().setAll(qv.getView());
    }

    private void showCodeExercisesForLesson() {
        if (currentLesson == null) return;
        CodeExerciseView cev = new CodeExerciseView(currentLesson.getId());
        cev.setOnNavigate(onNavigate);
        view.getChildren().setAll(cev.getView());
    }

    private Student getStudent() {
        try {
            return CurrentUser.getInstance().getStudent();
        }
        catch (Exception ignored) {
            return null;
        }
    }

    /** Zwraca główny kontener widoku lekcji. @return kontener widoku lekcji */
    public StackPane getView() { return view; }
}