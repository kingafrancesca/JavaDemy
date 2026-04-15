package com.example.javademy.ai;
import java.util.HashMap;
import java.util.Map;

/**
 * Przechowuje profil słabości ucznia.
 * Dla każdego tagu błędu śledzi liczbę wystąpień
 * oraz czy dany problem został już naprawiony.
 */
public class StudentWeakPoints
{
    private final Map<String, Integer> errorCounts;
    private final Map<String, Boolean> fixedTags;

    /** Liczba błędów tego samego typu, po której ErrorAnalyzer generuje ścieżkę naprawczą. */
    private static final int ERROR_LIMIT = 3;

    /** Tworzy instancję StudentWeakPoints z pustymi licznikami. */
    public StudentWeakPoints()
    {
        this.errorCounts = new HashMap<>();
        this.fixedTags = new HashMap<>();
    }

    /**
     * Rejestruje wystąpienie błędu dla danego tagu.
     * Jeśli tag pojawia się po raz pierwszy, inicjalizuje licznik.
     * Resetuje status naprawy, jeśli błąd wraca po naprawie.
     *
     * @param tag tag błędu do zarejestrowania
     */
    public void registerError(String tag)
    {
        errorCounts.put(tag, errorCounts.getOrDefault(tag, 0) + 1);
        fixedTags.put(tag, false);
    }

    /**
     * Sprawdza, czy dany tag przekroczył próg słabości.
     * Jeśli tak, ErrorAnalyzer powinien wygenerować ścieżkę naprawczą.
     *
     * @param tag tag błędu do sprawdzenia.
     * @return true, jeśli tag wymaga interwencji.
     */
    public boolean isWeakPoint(String tag) {
        return errorCounts.getOrDefault(tag, 0) >= ERROR_LIMIT && !fixedTags.getOrDefault(tag, false);
    }

    /**
     * Oznacza dany tag jako naprawiony.
     * Wywoływane, gdy uczeń poprawnie rozwiąże zadania naprawcze.
     *
     * @param tag tag błędu do oznaczenia jako naprawiony
     */
    public void markAsFixed(String tag)
    {
        fixedTags.put(tag, true);
    }

}