package com.example.javademy.ai;
import com.example.javademy.model.AttemptLog;
import com.example.javademy.model.Exercise;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * Główny silnik analizy błędów w aplikacji JavaDemy.
 * Analizuje błędy ucznia, wykrywa wzorce i generuje
 * spersonalizowane ścieżki naprawcze.
 */
public class ErrorAnalyzer
{
    private final ErrorDetector errorDetector;
    private final StudentWeakPoints weakPoints;
    private RepairPlan activeRepairPlan;

    /** Tworzy instancję ErrorAnalyzer z domyślnymi komponentami. */
    public ErrorAnalyzer() {
        this.errorDetector = new ErrorDetector();
        this.weakPoints = new StudentWeakPoints();
        this.activeRepairPlan = null;
    }

    /**
     * Analizuje wynik próby ucznia.
     * Jeśli odpowiedź była błędna, rejestruje tagi błędów
     * i sprawdza, czy potrzebna jest ścieżka naprawcza.
     *
     * @param attempt wynik próby ucznia
     * @param allExercises wszystkie dostępne zadania (do budowania ścieżki naprawczej)
     */
    public void analyzeAttempt(AttemptLog attempt, List<Exercise> allExercises)
    {
        if (attempt.correct()) {
            handleCorrectAnswer();
            return;
        }
        List<String> detectedTags;
        if (attempt.errorMessage() != null && !attempt.errorMessage().isEmpty()) detectedTags = errorDetector.detectErrors(attempt.errorMessage());
        else detectedTags = errorDetector.detectFromTags(attempt.detectedErrorTags());
        for (String tag : detectedTags)
            weakPoints.registerError(tag);
        startRepairIfNeeded(detectedTags, allExercises);
    }

    /**
     * Obsługuje poprawną odpowiedź ucznia.
     * Jeśli aktywna ścieżka naprawcza istnieje, przesuwa ją do przodu.
     * Sprawdza, czy słaby punkt został naprawiony.
     */
    private void handleCorrectAnswer()
    {
        if (activeRepairPlan != null && !activeRepairPlan.isCompleted()) {
            activeRepairPlan.moveToNext();
            if (activeRepairPlan.isCompleted()) {
                weakPoints.markAsFixed(activeRepairPlan.getTargetTag());
                activeRepairPlan = null;
            }
        }
    }

    /**
     * Sprawdza, czy wykryte tagi wymagają ścieżki naprawczej
     * i tworzy ją, jeśli potrzeba.
     *
     * @param detectedTags wykryte tagi błędów
     * @param allExercises wszystkie dostępne zadania
     */
    private void startRepairIfNeeded(List<String> detectedTags, List<Exercise> allExercises)
    {
        if (activeRepairPlan != null && !activeRepairPlan.isCompleted()) return;
        for (String tag : detectedTags)
        {
            if (weakPoints.isWeakPoint(tag))
            {
                RepairPlan plan = buildRepairPlan(tag, allExercises);
                if (plan != null)
                {
                    activeRepairPlan = plan;
                    return;
                }
            }
        }
    }

    /**
     * Buduje ścieżkę naprawczą dla danego tagu błędu.
     * Dobiera zadania posortowane od najprostszego do najtrudniejszego,
     * które zawierają dany tag błędu.
     *
     * @param tag tag błędu
     * @param allExercises wszystkie dostępne zadania
     * @return gotowa ścieżka naprawcza
     */
    private RepairPlan buildRepairPlan(String tag, List<Exercise> allExercises)
    {
        List<Exercise> matching = new ArrayList<>();
        for (Exercise exercise : allExercises)
        {
            for (String exerciseTag : exercise.getErrorTags())
            {
                if (exerciseTag.equals(tag))
                {
                    matching.add(exercise);
                    break;
                }
            }
        }
        if (matching.isEmpty()) return null;
        matching.sort(Comparator.comparingInt(Exercise::getDifficulty));
        RepairPlan plan = new RepairPlan(tag);
        for (Exercise exercise : matching)
            plan.addExercise(exercise);
        return plan;
    }

}