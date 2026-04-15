package com.example.javademy.model;

/**
 * Abstrakcyjna klasa bazowa reprezentująca zadanie w aplikacji JavaDemy.
 * Rozszerzana przez {@link QuizExercise} i {@link CodeExercise}.
 */
public abstract class Exercise
{
    private final String id;
    private final String content;
    private final String topicId;
    private final String lessonId;
    private final int difficulty;
    private final String[] errorTags;

    /**
     * Tworzy nowe zadanie z podanymi parametrami.
     *
     * @param id unikalny identyfikator
     * @param content treść zadania
     * @param topicId identyfikator tematu
     * @param lessonId identyfikator lekcji
     * @param difficulty poziom trudności
     * @param errorTags tagi błędów
     */
    public Exercise(String id, String content, String topicId, String lessonId, int difficulty, String[] errorTags) {
        this.id = id;
        this.content = content;
        this.topicId = topicId;
        this.lessonId = lessonId;
        this.difficulty = difficulty;
        this.errorTags = errorTags;
    }

    /**
     * Sprawdza, czy podana odpowiedź ucznia jest poprawna.
     *
     * @param answer odpowiedź lub output programu ucznia.
     * @return true, jeśli odpowiedź jest poprawna.
     */
    public abstract boolean checkAnswer(String answer);

    /**
     * Zwraca unikalny identyfikator zadania.
     *
     * @return unikalny identyfikator zadania
     */
    public String getId() {
        return id;
    }

    /**
     * Zwraca treść zadania.
     *
     * @return treść zadania
     */
    public String getContent() {
        return content;
    }

    /**
     * Zwraca identyfikator tematu.
     *
     * @return identyfikator tematu
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Zwraca identyfikator lekcji.
     *
     * @return identyfikator lekcji
     */
    public String getLessonId() {
        return lessonId;
    }

    /**
     * Zwraca poziom trudności.
     *
     * @return poziom trudności
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Zwraca tagi błędów powiązanych z zadaniem.
     *
     * @return tagi błędów powiązanych z zadaniem
     */
    public String[] getErrorTags() {
        return errorTags;
    }

}
