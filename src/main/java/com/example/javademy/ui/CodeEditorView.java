package com.example.javademy.ui;

import com.example.javademy.engine.CodeGuard;
import com.example.javademy.engine.CodeRunner;
import javafx.animation.ScaleTransition;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * Widok edytora kodu (Code Lab) w aplikacji JavaDemy.
 * Umożliwia uczniowi pisanie, kompilację i uruchamianie kodu Java
 * w czasie rzeczywistym. Wyniki wykonania wyświetlane są w panelu Output.
 */
public class CodeEditorView
{
    private static final String BG = "#F5ECE1";
    private static final String EDITOR_BG = "#EAE0D0";
    private static final String OUTPUT_BG = "#F0E8DC";
    private static final String TEXT1 = "#4B3D33";
    private static final String TEXT2 = "#8B7E74";
    private static final String BEIGE = "#B5A99A";
    private static final String BORDER_BEIGE = "#DDD0C0";
    private static final String SAGE = "#9FB395";
    private static final String STATUS_READY = "#B5A99A";
    private static final String STATUS_SUCCESS = "#9FB395";
    private static final String STATUS_ERROR = "#9A5B44";

    private static final double PANEL_INSET = 12.0;
    private static final double CONTENT_MARGIN = 18.0;

    private final CodeRunner codeRunner   = new CodeRunner();
    private TextArea codeEditor;
    private TextArea outputArea;
    private Label statusLabel;
    private Consumer<String> onNavigate;
    private final StackPane view = new StackPane();

    /**
     * Tworzy widok edytora kodu i buduje jego układ.
     */
    public CodeEditorView() {
        loadFonts();
        buildView();
    }

    /**
     * Ustawia callback nawigacji do innych widoków aplikacji.
     *
     * @param nav funkcja przyjmująca nazwę docelowego widoku.
     */
    public void setOnNavigate(Consumer<String> nav) { this.onNavigate = nav; }

    private void navigate(String dest) { if (onNavigate != null) onNavigate.accept(dest); }

    /** @return panel główny widoku */
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

    private void buildView() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG + ";");

        HBox navBar    = buildNavBar();
        HBox panelsRow = buildPanelsRow();
        HBox bottomRow = buildBottomRow();

        VBox.setVgrow(panelsRow, Priority.ALWAYS);

        root.getChildren().addAll(navBar, panelsRow, bottomRow);

        view.getChildren().setAll(root);
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

        Label lekcje = navItem("Lekcje", false);
        Label sep1 = navSep();
        Label codelab = navItem("Code Lab", true);
        lekcje.setOnMouseClicked(_ -> navigate("lekcje"));

        Label logo = new Label("JavaDemy");
        logo.setFont(Font.font("Playfair Display", 22));
        logo.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-cursor: hand;");
        logo.setOnMouseClicked(_ -> navigate("dashboard"));

        Label zadania = navItem("Zadania", false);
        Label sep2 = navSep();
        Label quizy = navItem("Quizy", false);
        zadania.setOnMouseClicked(_ -> navigate("zadania"));
        quizy.setOnMouseClicked(_ -> navigate("quizy"));

        nav.getChildren().addAll(sL, lekcje, sep1, codelab, sM1, logo, sM2, zadania, sep2, quizy, sR);
        return nav;
    }

    private Label navItem(String text, boolean active) {
        Label l = new Label(text);
        l.setFont(Font.font("JetBrains Mono", 13));
        if (active) l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-border-color: transparent transparent " + SAGE + " transparent; -fx-border-width: 0 0 2 0;");
        else {
            l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;");
            l.setOnMouseEntered(_ -> l.setStyle("-fx-text-fill: " + TEXT1 + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
            l.setOnMouseExited (_ -> l.setStyle("-fx-text-fill: " + BEIGE + "; -fx-padding: 0 10 0 10; -fx-cursor: hand;"));
        }
        return l;
    }

    private Label navSep() {
        Label l = new Label("|");
        l.setFont(Font.font("JetBrains Mono", 13));
        l.setStyle("-fx-text-fill: " + BORDER_BEIGE + "; -fx-padding: 0 2 0 2;");
        return l;
    }

    private HBox buildPanelsRow() {
        HBox row = new HBox(28);
        row.setAlignment(Pos.TOP_CENTER);
        row.setPadding(new Insets(24, 50, 16, 50));
        VBox.setVgrow(row, Priority.ALWAYS);

        codeEditor = new TextArea("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Witaj w CodeLab!");
                    }
                }""");
        codeEditor.setWrapText(false);
        codeEditor.setFont(Font.font("JetBrains Mono", 13));
        codeEditor.setStyle(editorAreaStyle(EDITOR_BG, TEXT1));
        codeEditor.getStyleClass().add("codelab-area");
        VBox.setVgrow(codeEditor, Priority.ALWAYS);

        String initCode = codeEditor.getText();
        int initLinesC = initCode.split("\n", -1).length;
        StringBuilder initLNC = new StringBuilder();
        for (int i = 1; i <= initLinesC; i++) {
            if (i > 1) initLNC.append("\n");
            initLNC.append(i);
        }
        TextArea lineNumbers = new TextArea(initLNC.toString());
        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setWrapText(false);
        lineNumbers.setPrefWidth(36);
        lineNumbers.setMinWidth(36);
        lineNumbers.setMaxWidth(36);
        lineNumbers.setStyle(editorAreaStyle(EDITOR_BG, "rgba(120,100,80,0.45)"));
        lineNumbers.getStyleClass().add("codelab-area");
        VBox.setVgrow(lineNumbers, Priority.ALWAYS);

        codeEditor.textProperty().addListener((_, _, text) -> {
            int n = text.split("\n", -1).length;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= n; i++) { if (i > 1) sb.append("\n"); sb.append(i); }
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
                int pos  = codeEditor.getCaretPosition();
                String   text = codeEditor.getText();
                int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
                String   line = text.substring(lineStart, pos);
                StringBuilder indent = new StringBuilder();
                for (char c : line.toCharArray()) {
                    if (c == ' ' || c == '\t') indent.append(c); else break;
                }
                codeEditor.insertText(pos, "\n" + indent);
            }
        });

        Label javaLabel = new Label("Java");
        javaLabel.setFont(Font.font("JetBrains Mono", 11));
        javaLabel.setStyle("-fx-text-fill: " + BEIGE + ";");

        HBox.setHgrow(codeEditor, Priority.ALWAYS);
        HBox editorWithLines = new HBox(0, lineNumbers, codeEditor);
        VBox.setVgrow(editorWithLines, Priority.ALWAYS);
        VBox editorContent = new VBox(8, javaLabel, editorWithLines);
        VBox.setVgrow(editorContent, Priority.ALWAYS);

        StackPane editorStack = buildLabPanel(EDITOR_BG, editorContent);
        HBox.setHgrow(editorStack, Priority.ALWAYS);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setFont(Font.font("JetBrains Mono", 13));
        outputArea.setStyle(editorAreaStyle(OUTPUT_BG, TEXT1));
        outputArea.getStyleClass().add("codelab-area");
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        Label outputLabel = new Label("Output:");
        outputLabel.setFont(Font.font("JetBrains Mono", 11));
        outputLabel.setStyle("-fx-text-fill: " + BEIGE + ";");

        VBox outputContent = new VBox(8, outputLabel, outputArea);
        VBox.setVgrow(outputContent, Priority.ALWAYS);

        StackPane outputStack = buildLabPanel(OUTPUT_BG, outputContent);
        outputStack.setPrefWidth(320);
        outputStack.setMinWidth(240);

        row.getChildren().addAll(editorStack, outputStack);
        return row;
    }

    private StackPane buildLabPanel(String innerBgColor, VBox content) {
        double arc1 = 36;
        double arc2 = 22;

        Rectangle outerGlass = new Rectangle();
        outerGlass.setManaged(false);
        outerGlass.setArcWidth(arc1); outerGlass.setArcHeight(arc1);
        outerGlass.setFill(Color.rgb(245, 238, 228, 0.50));

        InnerShadow glassHL    = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.30), 12, 0, -1, -1);
        InnerShadow glassDepth = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.04),        8, 0,  1,  1);
        glassDepth.setInput(glassHL);
        DropShadow  glassLift  = new DropShadow (BlurType.GAUSSIAN, Color.rgb(0,0,0,0.06),       12, 0,  0,  4);
        glassLift.setInput(glassDepth);
        outerGlass.setEffect(glassLift);

        Rectangle glassBorder = new Rectangle();
        glassBorder.setManaged(false);
        glassBorder.setArcWidth(arc1); glassBorder.setArcHeight(arc1);
        glassBorder.setFill(Color.TRANSPARENT);
        glassBorder.setStroke(Color.rgb(255, 255, 255, 0.40));
        glassBorder.setStrokeWidth(1);

        Rectangle innerArea = new Rectangle();
        innerArea.setManaged(false);
        innerArea.setLayoutX(PANEL_INSET);
        innerArea.setLayoutY(PANEL_INSET);
        innerArea.setArcWidth(arc2); innerArea.setArcHeight(arc2);
        innerArea.setFill(Color.web(innerBgColor));

        InnerShadow sunkenDark  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.09),       7, 0,  2,  2);
        InnerShadow sunkenLight = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 5, 0, -1, -1);
        sunkenLight.setInput(sunkenDark);
        innerArea.setEffect(sunkenLight);

        double m = PANEL_INSET + CONTENT_MARGIN;
        StackPane.setMargin(content, new Insets(m));
        StackPane.setAlignment(content, Pos.TOP_LEFT);

        StackPane panel = new StackPane(outerGlass, glassBorder, innerArea, content);

        panel.widthProperty().addListener((_, _, w) -> {
            double pw = w.doubleValue();
            outerGlass .setWidth(pw);
            glassBorder.setWidth(pw);
            innerArea  .setWidth(Math.max(0, pw - PANEL_INSET * 2));
        });
        panel.heightProperty().addListener((_, _, h) -> {
            double ph = h.doubleValue();
            outerGlass .setHeight(ph);
            glassBorder.setHeight(ph);
            innerArea  .setHeight(Math.max(0, ph - PANEL_INSET * 2));
        });

        return panel;
    }

    private String editorAreaStyle(String bg, String fg) {
        return  "-fx-control-inner-background: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;";
    }

    private HBox buildBottomRow() {
        statusLabel = new Label("Gotowy do pracy");
        statusLabel.setFont(Font.font("JetBrains Mono", 13));
        statusLabel.setStyle("-fx-text-fill: " + STATUS_READY + ";");

        Button clearBtn = new Button("Wyczyść");
        clearBtn.setFont(Font.font("JetBrains Mono", 12));
        String clearDef = "-fx-background-color:rgba(245,238,228,0.7); -fx-background-radius:18; -fx-border-color:rgba(200,190,178,0.55); -fx-border-radius:18;-fx-border-width:1; -fx-text-fill:" + TEXT2 + "; -fx-padding:8 20;-fx-cursor:hand;";
        String clearHov = "-fx-background-color:rgba(245,238,228,0.95); -fx-background-radius:18; -fx-border-color:" + BORDER_BEIGE + "; -fx-border-radius:18;-fx-border-width:1; -fx-text-fill:" + TEXT1 + "; -fx-padding:8 20;-fx-cursor:hand;";
        clearBtn.setStyle(clearDef);
        clearBtn.setOnMouseEntered(_ -> clearBtn.setStyle(clearHov));
        clearBtn.setOnMouseExited (_ -> clearBtn.setStyle(clearDef));
        clearBtn.setOnAction(_ -> {
            codeEditor.clear();
            outputArea.setStyle(editorAreaStyle(OUTPUT_BG, TEXT1));
            outputArea.clear();
            setStatus("Gotowy do pracy", STATUS_READY);
        });
        addHoverScale(clearBtn);

        Button runBtn = new Button("▶  Uruchom");
        runBtn.setFont(Font.font("JetBrains Mono", 12));
        runBtn.setStyle("-fx-background-color:" + SAGE + "; -fx-background-radius:18; -fx-text-fill:white; -fx-padding:8 22;-fx-cursor:hand;");
        InnerShadow clayHL  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(255,255,255,0.35), 8, 0, -2, -2);
        InnerShadow claySh  = new InnerShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.15),       6, 0,  2,  2);
        claySh.setInput(clayHL);
        DropShadow  clayDrop = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.12), 8, 0, 0, 4);
        clayDrop.setInput(claySh);
        runBtn.setEffect(clayDrop);
        runBtn.setOnAction(_ -> runCode());
        addHoverScale(runBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(20, statusLabel, clearBtn, runBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(0, spacer, buttons);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(14, 50, 24, 50));

        return row;
    }

    private void addHoverScale(Button btn) {
        btn.setOnMouseEntered(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), btn);
            st.setToX(1.04); st.setToY(1.04); st.play();
        });
        btn.setOnMouseExited(_ -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), btn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void setStatus(String text, String color) {
        statusLabel.setText(text);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    private void runCode() {
        String code = codeEditor.getText().trim();
        if (code.isEmpty()) {
            outputArea.setStyle(editorAreaStyle(OUTPUT_BG, STATUS_ERROR));
            outputArea.setText("Edytor jest pusty!");
            setStatus("Błąd kompilacji", STATUS_ERROR);
            return;
        }

        CodeGuard.ExecutionResult result = codeRunner.compileAndRun(code);

        if (result.success()) {
            outputArea.setStyle(editorAreaStyle(OUTPUT_BG, TEXT1));
            outputArea.setText(result.output());
            setStatus("Wykonano pomyślnie", STATUS_SUCCESS);
        }
        else {
            CodeRunner.CompilationResult comp = codeRunner.compile(code);
            String errorText = !comp.success() ? comp.errorMessage() : result.output();
            outputArea.setStyle(editorAreaStyle(OUTPUT_BG, STATUS_ERROR));
            outputArea.setText(errorText);
            setStatus("Błąd kompilacji", STATUS_ERROR);
        }
    }
}
