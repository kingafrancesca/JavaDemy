package com.example.javademy.model;
import java.util.List;

/**
 * Reprezentuje zadanie quizowe w aplikacji JavaDemy.
 * Obsługuje quizy z jednokrotnym wyborem odpowiedzi.
 */
public class QuizExercise extends Exercise
{
    private final List<String> options;
    private final String correctAnswer;

    /**
     * Tworzy nowe zadanie quizowe.
     *
     * @param id unikalny identyfikator zadania
     * @param content treść pytania
     * @param topicId identyfikator tematu
     * @param lessonId identyfikator lekcji
     * @param difficulty poziom trudności
     * @param errorTags tagi błędów powiązanych z pytaniem
     * @param options lista opcji odpowiedzi
     * @param correctAnswer poprawna odpowiedź
     */
    public QuizExercise(String id, String content, String topicId, String lessonId, int difficulty, String[] errorTags, List<String> options, String correctAnswer) {
        super(id, content, topicId, lessonId, difficulty, errorTags);
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Sprawdza, czy odpowiedź ucznia jest poprawna (bez rozróżniania wielkości liter).
     *
     * @param answer odpowiedź wybrana przez ucznia.
     * @return true, jeśli odpowiedź zgadza się z poprawną.
     */
    @Override
    public boolean checkAnswer(String answer)
    {
        if (answer == null) return false;
        return answer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    /** Zwraca listę opcji odpowiedzi. @return lista opcji odpowiedzi */
    public List<String> getOptions() {
        return options;
    }

    /** Zwraca poprawną odpowiedź. @return poprawna odpowiedź */
    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
