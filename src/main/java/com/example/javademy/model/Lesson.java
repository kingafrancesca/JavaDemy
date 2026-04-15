package com.example.javademy.model;
import java.util.List;
import java.util.ArrayList;

/**
 * Reprezentuje lekcję w aplikacji JavaDemy.
 * Każda lekcja składa się z kroków ({@link LessonStep}),
 * z których każdy zawiera tekst i opcjonalne przykłady kodu.
 */
public class Lesson
{
    private final String id;
    private final String title;
    private final String topicId;
    private final List<LessonStep> steps;

    /**
     * Tworzy nową lekcję z pustą listą kroków.
     *
     * @param id unikalny identyfikator lekcji
     * @param title tytuł wyświetlany uczniowi
     * @param topicId identyfikator tematu nadrzędnego
     */
    public Lesson(String id, String title, String topicId) {
        this.id = id; this.title = title;
        this.topicId = topicId;
        this.steps = new ArrayList<>();
    }

    /**
     * Reprezentuje pojedynczy przykład kodu w kroku lekcji.
     *
     * @param description opis przykładu
     * @param code        kod Java
     */
    public record CodeExample(String description, String code) {}

    /**
     * Pojedynczy krok (rozdział) lekcji.
     * Zawiera tytuł, tekst treści i listę przykładów kodu.
     */
    public static class LessonStep
    {
        private final String title;
        private final String content;
        private final List<CodeExample> codeExamples;

        /**
         * Identyfikator sekcji (rozdziału), np. "java-intro-1.1".
         */
        private String sectionId;

        /**
         * Tworzy krok lekcji z listą przykładów kodu.
         *
         * @param title tytuł kroku
         * @param content treść kroku
         * @param codeExamples lista przykładów kodu lub null
         */
        public LessonStep(String title, String content, List<CodeExample> codeExamples)
        {
            this.title = title;
            this.content = content;
            this.codeExamples = codeExamples != null ? codeExamples : new ArrayList<>();
        }

        /**
         * Tworzy krok lekcji z pojedynczym przykładem kodu.
         *
         * @param title tytuł kroku
         * @param content treść kroku
         * @param codeExample pojedynczy przykład kodu lub null
         */
        public LessonStep(String title, String content, String codeExample)
        {
            this.title = title;
            this.content = content;
            this.codeExamples = new ArrayList<>();
            if (codeExample != null && !codeExample.isBlank())
                this.codeExamples.add(new CodeExample("", codeExample));
        }

        /**
         * Zwraca tytuł kroku.
         *
         * @return tytuł kroku
         */
        public String getTitle() {
            return title;
        }

        /**
         * Zwraca treść kroku.
         *
         * @return treść kroku
         * */
        public String getContent() {
            return content;
        }

        /**
         * Zwraca listę przykładów kodu.
         *
         * @return lista przykładów kodu
         */
        public List<CodeExample> getCodeExamples() {
            return codeExamples;
        }

        /**
         * Zwraca identyfikator sekcji.
         *
         * @return identyfikator sekcji lub null
         */
        public String getSectionId() {
            return sectionId;
        }

        /**
         * Przypisuje krok do sekcji lekcji.
         *
         * @param id identyfikator sekcji, np. "java-intro-1.1".
         */
        public void setSectionId(String id) {
            this.sectionId = id;
        }
    }

    /**
     * Dodaje krok na koniec lekcji.
     *
     * @param step krok do dodania
     */
    public void addStep(LessonStep step) {
        steps.add(step);
    }

    /**
     * Zwraca identyfikator lekcji.
     *
     * @return identyfikator lekcji
     */
    public String getId() {
        return id;
    }

    /**
     * Zwraca tytuł lekcji.
     *
     * @return tytuł lekcji
     */
    public String getTitle() {
        return title;
    }

    /**
     * Zwraca identyfikator tematu nadrzędnego.
     *
     * @return identyfikator tematu nadrzędnego
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Zwraca listę kroków lekcji.
     *
     * @return lista kroków lekcji
     */
    public List<LessonStep> getSteps() {
        return steps;
    }

    /**
     * Zwraca liczbę kroków w lekcji.
     *
     * @return liczba kroków w lekcji
     */
    public int getStepCount() {
        return steps.size();
    }
}
