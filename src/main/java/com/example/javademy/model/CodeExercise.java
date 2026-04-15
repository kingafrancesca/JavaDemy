package com.example.javademy.model;

/**
 * Reprezentuje zadanie praktyczne z edytorem kodu.
 * Uczeń pisze kod Java, który jest kompilowany i sprawdzany
 * przez {@link com.example.javademy.engine.TestRunner}.
 */
public class CodeExercise extends Exercise
{
    private final String starterCode;
    private final String testCode;
    private final String expectedOutput;
    private final String hint;

    /**
     * Tworzy nowe zadanie kodowe.
     *
     * @param id unikalny identyfikator zadania
     * @param content treść zadania wyświetlana uczniowi
     * @param topicId identyfikator tematu
     * @param lessonId identyfikator lekcji
     * @param difficulty poziom trudności (skala 1-3 gwiazdek)
     * @param errorTags tagi błędów powiązanych z zadaniem
     * @param starterCode kod startowy w edytorze
     * @param testCode kod testów jednostkowych lub null
     * @param expectedOutput oczekiwany output lub null
     * @param hint podpowiedź lub null
     */
    public CodeExercise(String id, String content, String topicId, String lessonId, int difficulty, String[] errorTags, String starterCode, String testCode, String expectedOutput, String hint) {
        super(id, content, topicId, lessonId, difficulty, errorTags);
        this.starterCode = starterCode;
        this.testCode = testCode;
        this.expectedOutput = expectedOutput;
        this.hint = hint;
    }

    /**
     * Sprawdza odpowiedź ucznia przez porównanie z oczekiwanym outputem.
     *
     * @param answer output programu ucznia.
     * @return true, jeśli output zgadza się z oczekiwanym.
     */
    @Override
    public boolean checkAnswer(String answer) {
        if (answer == null || expectedOutput == null) return false;
        return answer.trim().equals(expectedOutput.trim());
    }

    /**
     * Zwraca kod startowy wyświetlany w edytorze.
     *
     * @return kod startowy wyświetlany w edytorze
     */
    public String getStarterCode() {
        return starterCode;
    }

    /**
     * Zwraca kod testów jednostkowych.
     *
     * @return kod testów jednostkowych lub null
     */
    public String getTestCode() {
        return testCode;
    }

    /**
     * Zwraca oczekiwany output programu.
     *
     * @return oczekiwany output programu lub null
     */
    public String getExpectedOutput() {
        return expectedOutput;
    }

    /**
     * Zwraca podpowiedź dla ucznia.
     *
     * @return podpowiedź dla ucznia lub null
     */
    public String getHint() {
        return hint;
    }
}
