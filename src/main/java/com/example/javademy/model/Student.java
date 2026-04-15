package com.example.javademy.model;
import java.time.LocalDate;
import java.util.*;

/**
 * Reprezentuje profil ucznia w aplikacji JavaDemy.
 * Przechowuje dane logowania, postęp lekcji, wyniki quizów,
 * serię dni nauki oraz słabe punkty wymagające dodatkowej powtórki materiału.
 */
public class Student
{
    private final String login;
    private String passwordHash;
    private String displayName;
    private String lastLoginDate;
    private int loginStreak;
    private Set<String> completedSteps;
    private Map<String, Double> quizScores;
    private Map<String, Integer> weakExercises;

    /**
     * Tworzy nowe konto ucznia z zerowymi statystykami.
     *
     * @param login unikalny login
     * @param passwordHash hash hasła SHA-256
     * @param displayName imię wyświetlane w aplikacji
     */
    public Student(String login, String passwordHash, String displayName)
    {
        this.login = login;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.lastLoginDate = null;
        this.loginStreak = 0;
        this.completedSteps = new HashSet<>();
        this.quizScores = new HashMap<>();
    }

    /**
     * Oznacza krok lekcji jako ukończony.
     *
     * @param sectionId identyfikator sekcji do oznaczenia
     */
    public void completeStep(String sectionId)
    {
        safeSteps().add(sectionId);
    }

    /**
     * Sprawdza, czy dany krok lekcji został ukończony.
     *
     * @param sectionId identyfikator sekcji do sprawdzenia.
     * @return true, jeśli krok jest ukończony.
     */
    public boolean isStepCompleted(String sectionId)
    {
        return safeSteps().contains(sectionId);
    }

    /**
     * Oblicza postęp lekcji jako 0.0-1.0.
     * Określa, ile z podanych sekcji zostało przez ucznia ukończonych.
     *
     * @param sectionIds lista identyfikatorów sekcji należących do lekcji.
     * @return postęp od 0.0 (żadna sekcja nierozpoczęta) do 1.0 (wszystkie sekcje ukończone).
     */
    public double getLessonProgress(List<String> sectionIds)
    {
        if (sectionIds == null || sectionIds.isEmpty()) return 0.0;
        long done = sectionIds.stream().filter(this::isStepCompleted).count();
        return (double) done / sectionIds.size();
    }

    /**
     * Zapisuje wynik quizu dla sekcji, pod warunkiem że jest lepszy od poprzedniego.
     *
     * @param sectionId identyfikator sekcji
     * @param score wynik do zapisania (0.0-1.0).
     */
    public void updateQuizScore(String sectionId, double score)
    {
        double current = safeScores().getOrDefault(sectionId, 0.0);
        if (score > current) safeScores().put(sectionId, Math.min(1.0, score));
    }

    /**
     * Zwraca najlepszy wynik quizu dla danej sekcji.
     *
     * @param sectionId identyfikator sekcji
     * @return wynik od 0.0 do 1.0 (0.0 jeśli quiz nie był rozwiązany).
     */
    public double getQuizScore(String sectionId)
    {
        return safeScores().getOrDefault(sectionId, 0.0);
    }

    /**
     * Aktualizuje serię kolejnych dni nauki na podstawie daty logowania.
     * Seria rośnie o 1 przy logowaniu w kolejnym dniu;
     * resetuje się, jeśli przerwa była dłuższa niż jeden dzień.
     */
    public void updateLoginStreak()
    {
        String today = LocalDate.now().toString();
        if (lastLoginDate == null || lastLoginDate.isBlank()) {
            loginStreak = 1; lastLoginDate = today;
            return;
        }
        if (today.equals(lastLoginDate)) return;
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(lastLoginDate), LocalDate.parse(today));
        loginStreak   = (days == 1) ? loginStreak + 1 : 1;
        lastLoginDate = today;
    }

    private Set<String> safeSteps()
    {
        if (completedSteps == null) completedSteps = new HashSet<>();
        return completedSteps;
    }

    private Map<String, Double> safeScores()
    {
        if (quizScores == null) quizScores = new HashMap<>();
        return quizScores;
    }

    private Map<String, Integer> safeWeakPoints()
    {
        if (weakExercises == null) weakExercises = new HashMap<>();
        return weakExercises;
    }

    public String getLogin() {
        return login;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public int getLoginStreak() {
        return loginStreak;
    }

    /**
     * Dodaje zadanie do listy słabych punktów ucznia.
     * Nie robi nic, jeśli zadanie już istnieje na liście.
     *
     * @param exerciseId identyfikator zadania
     */
    public void addWeakExercise(String exerciseId)
    {
        safeWeakPoints().putIfAbsent(exerciseId, 0);
    }

    /**
     * Sprawdza, czy dane zadanie jest aktywnym słabym punktem ucznia.
     *
     * @param exerciseId identyfikator zadania
     * @return true, jeśli zadanie jest na liście słabych punktów
     */
    public boolean isWeakExercise(String exerciseId)
    {
        return safeWeakPoints().containsKey(exerciseId);
    }

    /**
     * Rejestruje poprawną odpowiedź na pytanie będące słabym punktem.
     * Po 2 poprawnych odpowiedziach usuwa zadanie ze słabych punktów.
     *
     * @param exerciseId identyfikator zadania
     */
    public void recordCorrect(String exerciseId)
    {
        if (!isWeakExercise(exerciseId)) return;
        int count = safeWeakPoints().get(exerciseId) + 1;
        if (count >= 2)
        {
            safeWeakPoints().remove(exerciseId);
            return;
        }
        safeWeakPoints().put(exerciseId, count);
    }

    /**
     * Zwraca kopię zbioru identyfikatorów aktywnych słabych punktów.
     *
     * @return zbiór identyfikatorów zadań będących słabymi punktami
     */
    public Set<String> getWeakExerciseIds()
    {
        return new HashSet<>(safeWeakPoints().keySet());
    }

    /**
     * Zwraca mapę słabych punktów ucznia.
     *
     * @return mapa słabych punktów: identyfikator zadania -&gt; liczba poprawnych odpowiedzi
     */
    public Map<String, Integer> getWeakExercises() {
        return safeWeakPoints();
    }

}
