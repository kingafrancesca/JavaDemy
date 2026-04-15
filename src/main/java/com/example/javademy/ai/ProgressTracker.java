package com.example.javademy.ai;
import com.example.javademy.auth.CurrentUser;
import com.example.javademy.model.*;
import com.example.javademy.storage.DataStorage;
import java.util.*;

/**
 * Śledzi postęp nauki ucznia na podstawie ukończonych kroków lekcji.
 * Oblicza procent ukończenia dla poszczególnych tematów oraz ogólny postęp.
 */
public class ProgressTracker
{
    private final DataStorage dataStorage;

    /** Tworzy instancję ProgressTracker. */
    public ProgressTracker() {
        this.dataStorage = new DataStorage();

    }

    /**
     * Oblicza postęp ucznia w danym temacie jako wartość 0.0-1.0.
     * Uwzględnia wszystkie lekcje należące do tematu.
     *
     * @param topicId identyfikator tematu
     * @return postęp od 0.0 (nierozpoczęty) do 1.0 (ukończony)
     */
    public double getTopicProgress(String topicId)
    {
        Student s = getStudent();
        if (s == null) return 0.0;
        List<Lesson> lessons = dataStorage.loadLessons();
        List<Lesson> tl = new ArrayList<>();
        for (Lesson l : lessons)
            if (topicId.equals(l.getTopicId())) tl.add(l);
        if (tl.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Lesson l : tl)
            sum += s.getLessonProgress(getSectionIds(l));
        return sum / tl.size();
    }

    /**
     * Oblicza ogólny postęp ucznia jako średnią ze wszystkich tematów.
     *
     * @return postęp od 0.0 (brak) do 1.0 (wszystko ukończone)
     */
    public double getOverallProgress()
    {
        List<Topic> topics = dataStorage.loadTopics();
        if (topics.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Topic t : topics)
            sum += getTopicProgress(t.getId());
        return sum / topics.size();
    }

    /**
     * Zwraca listę unikalnych identyfikatorów sekcji z kroków lekcji.
     *
     * @param lesson lekcja, z której pobierane są sekcje
     * @return lista identyfikatorów sekcji w kolejności wystąpienia
     */
    public static List<String> getSectionIds(Lesson lesson)
    {
        List<String> ids = new ArrayList<>();
        for (Lesson.LessonStep step : lesson.getSteps())
        {
            String sid = step.getSectionId();
            if (sid != null && !sid.isBlank() && !ids.contains(sid)) ids.add(sid);
        }
        return ids;
    }

    /**
     * Pobiera obiekt aktualnie zalogowanego ucznia.
     * Zwraca null, jeśli nikt nie jest zalogowany lub wystąpił błąd.
     *
     * @return zalogowany uczeń lub null
     */
    private Student getStudent()
    {
        try {
            return CurrentUser.getInstance().getStudent();
        }
        catch (Exception e) {
            return null;
        }
    }

}
