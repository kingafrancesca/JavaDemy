package com.example.javademy.engine;

/**
 * Sprawdza poprawność rozwiązania ucznia.
 * Obsługuje dwa tryby weryfikacji:
 * <ul>
 *   <li>Tryb 1: porównanie outputu programu z oczekiwanym wynikiem.</li>
 *   <li>Tryb 2: testy jednostkowe wstrzykiwane do klasy ucznia.</li>
 * </ul>
 */
public class TestRunner
{
    private final CodeRunner codeRunner;

    /**
     * Konstruktor TestRunner z domyślnym CodeRunner.
     */
    public TestRunner() {
        this.codeRunner = new CodeRunner();
    }

    /**
     * Kompiluje i uruchamia kod ucznia, a następnie porównuje output
     * z oczekiwanym wynikiem.
     *
     * @param studentCode kod Java napisany przez ucznia
     * @param expectedOutput oczekiwany output programu
     * @return wynik sprawdzenia zawierający status, błąd i rzeczywisty output.
     */
    public TestResult check(String studentCode, String expectedOutput)
    {
        CodeGuard.ExecutionResult exec = codeRunner.compileAndRun(studentCode);
        if (!exec.success()) return new TestResult(false, exec.errorMessage(), null);
        boolean passed = exec.output().trim().equals(expectedOutput.trim());
        return new TestResult(passed, null, exec.output());
    }

    /**
     * Dodaje metodę {@code runTests()} do klasy ucznia i uruchamia testy jednostkowe.
     * Zadanie jest niezaliczone, jeśli output zawiera "FAIL" lub "ERROR".
     *
     * <p>Oczekiwany format parametru {@code testCode}:</p>
     * <pre>
     * static String runTests() {
     *     StringBuilder sb = new StringBuilder();
     *     boolean t1 = metoda(arg) == oczekiwany;
     *     sb.append("Test 1: opis -&gt; ").append(t1 ? "OK" : "FAIL").append("\n");
     *     return sb.toString();
     * }
     * </pre>
     *
     * @param studentCode kod Java napisany przez ucznia
     * @param testCode kod testów jednostkowych do wstrzyknięcia
     * @return wynik testów zawierający status, błąd i output testów
     */
    public TestResult runTests(String studentCode, String testCode)
    {
        if (testCode == null || testCode.isBlank()) return new TestResult(false, "Brak testów dla tego zadania.", null);

        String combined = injectTests(studentCode, testCode);
        CodeGuard.ExecutionResult exec = codeRunner.compileAndRun(combined);

        if (!exec.success()) return new TestResult(false, exec.errorMessage(), null);

        String output = exec.output();
        boolean passed = !output.contains("FAIL") && !output.contains("ERROR");
        return new TestResult(passed, null, output);
    }

    /**
     * Dodaje {@code runTests()} do klasy StudentSolution
     * i dodaje metodę {@code main()}, która ją wywołuje.
     * Jeśli uczeń ma już własną metodę {@code main()} (zadania z lekcji 1-5),
     * zostaje ona zmieniona na {@code __studentMain__}, żeby uniknąć konfliktu.
     *
     * @param studentCode kod ucznia
     * @param testCode kod testów jednostkowych
     * @return scalony kod gotowy do kompilacji i uruchomienia
     */
    private String injectTests(String studentCode, String testCode)
    {
        String base = studentCode.trim();

        boolean hasStudentMain = base.contains("void main(");
        if (hasStudentMain) {
            base = base.replace("void main(", "void __studentMain__(");
            testCode = testCode.replace("main(new String", "__studentMain__(new String");
        }

        int lastBrace = base.lastIndexOf('}');
        if (lastBrace >= 0) base = base.substring(0, lastBrace);

        String escapedCode = studentCode.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");

        return base + "\n\n" +
                "    static String _sourceCode = \"" + escapedCode + "\";\n\n" +
                testCode + "\n\n" +
                "    public static void main(String[] args) {\n" +
                "        try {\n" +
                "            System.out.println(runTests());\n" +
                "        } catch (Exception e) {\n" +
                "            System.out.println(\"ERROR: \" + e.getMessage());\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    /**
     * Wynik sprawdzenia rozwiązania ucznia.
     *
     * @param passed czy zadanie zostało zaliczone?
     * @param errorMessage komunikat o błędzie kompilacji lub wykonania, lub null, jeśli brak błędu.
     * @param testOutput output testów lub programu, lub null, jeśli nie uruchomiono.
     */
    public record TestResult(boolean passed, String errorMessage, String testOutput) {}
}
