package com.example.javademy.model;

/**
 * Reprezentuje wynik próby rozwiązania zadania przez ucznia.
 * Używana przez silnik analizy błędów i śledzenia postępów.
 *
 * @param exerciseId        identyfikator zadania
 * @param correct           czy odpowiedź była poprawna
 * @param detectedErrorTags tagi wykrytych błędów
 * @param errorMessage      komunikat błędu lub null
 */
public record AttemptLog(
        String exerciseId,
        boolean correct,
        String[] detectedErrorTags,
        String errorMessage
) {}
