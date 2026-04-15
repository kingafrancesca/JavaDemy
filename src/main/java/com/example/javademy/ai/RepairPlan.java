package com.example.javademy.ai;
import com.example.javademy.model.Exercise;
import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje spersonalizowaną ścieżkę naprawczą dla ucznia.
 * Zawiera listę zadań dobranych celowo pod wykryty słaby punkt.
 */
public class RepairPlan
{
    private final String targetTag;
    private final List<Exercise> exercises;
    private int currentIndex;
    private boolean completed;

    /**
     * Konstruktor ścieżki naprawczej
     *
     * @param targetTag tag błędu, którego dotyczy ścieżka
     */
    public RepairPlan(String targetTag) {
        this.targetTag = targetTag;
        this.exercises = new ArrayList<>();
        this.currentIndex = 0;
        this.completed = false;
    }

    /**
     * Dodaje zadanie do ścieżki naprawczej.
     *
     * @param exercise zadanie do dodania
     */
    public void addExercise(Exercise exercise) {
        exercises.add(exercise);
    }


    /**
     * Przechodzi do następnego zadania w ścieżce.
     * Oznacza ścieżkę jako ukończoną, jeśli nie ma więcej zadań.
     */
    public void moveToNext() {
        currentIndex++;
        if (currentIndex >= exercises.size()) completed = true;
    }

    /**
     * Zwraca tag błędu, którego dotyczy ścieżka.
     *
     * @return tag błędu, którego dotyczy ścieżka
     */
    public String getTargetTag() {
        return targetTag;
    }

    /**
     * Sprawdza, czy ścieżka naprawcza jest ukończona.
     *
     * @return true, jeśli ścieżka ukończona
     */
    public boolean isCompleted() {
        return completed;
    }
}