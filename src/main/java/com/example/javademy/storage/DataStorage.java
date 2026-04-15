package com.example.javademy.storage;
import com.example.javademy.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

/**
 * Odpowiada za zapis i odczyt danych aplikacji z plików JSON.
 * Przechowuje tematy, lekcje, zadania oraz historię prób uczniów
 * w katalogu {@code data/}.
 */
public class DataStorage
{
    private static final String DATA_DIR = "data";
    private static final String TOPICS_FILE = DATA_DIR + "/topics.json";
    private static final String LESSONS_FILE = DATA_DIR + "/lessons.json";
    private static final String EXERCISES_FILE = DATA_DIR + "/exercises.json";
    private static final String ATTEMPTS_DIR = DATA_DIR + "/attempts";

    private final Gson gson;

    /**
     * Rejestruje deserializery Gson i tworzy wymagane katalogi, jeśli jeszcze nie istnieją.
     */
    public DataStorage()
    {
        this.gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Exercise.class, new ExerciseDeserializer()).registerTypeAdapter(Lesson.class,     new LessonDeserializer()).create();
        createDirectoriesIfNotExist();
    }

    private void createDirectoriesIfNotExist()
    {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(ATTEMPTS_DIR));
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można utworzyć folderów na dane", e);
        }
    }

    /**
     * Wczytuje listę tematów z pliku JSON.
     *
     * @return lista tematów lub pusta lista, jeśli plik nie istnieje.
     */
    public List<Topic> loadTopics()
    {
        Type type = new TypeToken<List<Topic>>(){}.getType();
        return loadFromFile(TOPICS_FILE, type);
    }

    /**
     * Wczytuje listę lekcji z pliku JSON.
     *
     * @return lista lekcji lub pusta lista, jeśli plik nie istnieje.
     */
    public List<Lesson> loadLessons()
    {
        Type type = new TypeToken<List<Lesson>>(){}.getType();
        return loadFromFile(LESSONS_FILE, type);
    }

    /**
     * Wczytuje wszystkie zadania z pliku JSON.
     *
     * @return lista wszystkich zadań lub pusta lista, jeśli plik nie istnieje.
     */
    public List<Exercise> loadExercises()
    {
        Type type = new TypeToken<List<Exercise>>(){}.getType();
        return loadFromFile(EXERCISES_FILE, type);
    }

    /**
     * Wczytuje tylko zadania kodowe.
     *
     * @return lista zadań typu {@link CodeExercise}.
     */
    public List<CodeExercise> loadCodeExercises()
    {
        List<CodeExercise> result = new ArrayList<>();
        for (Exercise ex : loadExercises())
            if (ex instanceof CodeExercise ce) result.add(ce);
        return result;
    }

    /**
     * Wczytuje quizy należące do lekcji o podanym identyfikatorze.
     * Wyszukuje quizy po topicId lekcji.
     *
     * @param lessonId identyfikator lekcji
     * @return lista quizów należących do tematu tej lekcji.
     */
    public List<QuizExercise> loadQuizExercisesForLesson(String lessonId) {
        List<Lesson> lessons = loadLessons();
        String topicId = null;
        for (Lesson l : lessons)
            if (lessonId.equals(l.getId())) { topicId = l.getTopicId(); break; }
        if (topicId == null) return new ArrayList<>();
        final String tid = topicId;
        List<QuizExercise> result = new ArrayList<>();
        for (Exercise ex : loadExercises())
            if (ex instanceof QuizExercise qe && tid.equals(qe.getTopicId())) result.add(qe);
        return result;
    }

    /**
     * Zapisuje historię prób ucznia do pliku JSON.
     *
     * @param login    login ucznia
     * @param attempts lista prób do zapisania
     */
    public void saveAttempts(String login, List<AttemptLog> attempts)
    {
        saveToFile(ATTEMPTS_DIR + "/" + login + "_attempts.json", attempts);
    }

    /**
     * Wczytuje historię prób ucznia z pliku JSON.
     *
     * @param login login ucznia
     * @return lista prób lub pusta lista, jeśli brak historii.
     */
    public List<AttemptLog> loadAttempts(String login)
    {
        Type type = new TypeToken<List<AttemptLog>>(){}.getType();
        return loadFromFile(ATTEMPTS_DIR + "/" + login + "_attempts.json", type);
    }

    private void saveToFile(String filePath, Object data)
    {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można zapisać: " + filePath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T loadFromFile(String filePath, Type type)
    {
        File file = new File(filePath);
        if (!file.exists()) return (T) new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można wczytać: " + filePath, e);
        }
    }

    private static class ExerciseDeserializer implements JsonDeserializer<Exercise> {
        @Override
        public Exercise deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject o = json.getAsJsonObject();

            String id = str(o, "id");
            String content = str(o, "content");
            String topicId = str(o, "topicId");
            String lessonId = str(o, "lessonId");
            int    difficulty = o.has("difficulty") ? o.get("difficulty").getAsInt() : 1;
            String type = str(o, "type");

            String[] errorTags = new String[0];
            if (o.has("errorTags") && o.get("errorTags").isJsonArray()) {
                JsonArray arr = o.getAsJsonArray("errorTags");
                errorTags = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++)
                    errorTags[i] = arr.get(i).getAsString();
            }

            if ("code".equals(type)) {
                String starterCode = str(o, "starterCode");
                String testCode = str(o, "testCode");
                String hint = str(o, "hint");
                return new CodeExercise(id, content, topicId, lessonId, difficulty, errorTags, starterCode, testCode, null, hint);
            }
            else {
                List<String> options = new ArrayList<>();
                if (o.has("options") && o.get("options").isJsonArray())
                    for (JsonElement el : o.getAsJsonArray("options"))
                        options.add(el.getAsString());

                String correctAnswer = str(o, "correctAnswer");
                return new QuizExercise(id, content, topicId, lessonId, difficulty, errorTags, options, correctAnswer);
            }
        }

        private String str(JsonObject o, String key) {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
        }
    }

    private static class LessonDeserializer implements JsonDeserializer<Lesson>
    {
        @Override
        public Lesson deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException
        {
            JsonObject o = json.getAsJsonObject();
            String id = str(o, "id");
            String title = str(o, "title");
            String topicId = str(o, "topicId");

            Lesson lesson = new Lesson(id, title, topicId);

            if (o.has("steps") && o.get("steps").isJsonArray()) {
                for (JsonElement stepEl : o.getAsJsonArray("steps")) {
                    JsonObject s = stepEl.getAsJsonObject();
                    String stepTitle = str(s, "title");
                    String stepContent = str(s, "content");
                    String sectionId = str(s, "sectionId");

                    List<Lesson.CodeExample> examples = new ArrayList<>();
                    if (s.has("codeExamples") && s.get("codeExamples").isJsonArray()) {
                        for (JsonElement exEl : s.getAsJsonArray("codeExamples")) {
                            JsonObject ex  = exEl.getAsJsonObject();
                            String desc = str(ex, "description");
                            String code = str(ex, "code");
                            examples.add(new Lesson.CodeExample(desc, code));
                        }
                    }
                    else if (s.has("codeExample") && !s.get("codeExample").isJsonNull()) {
                        String code = s.get("codeExample").getAsString();
                        if (!code.isBlank()) examples.add(new Lesson.CodeExample("", code));
                    }

                    Lesson.LessonStep newStep = new Lesson.LessonStep(stepTitle, stepContent, examples);
                    if (!sectionId.isBlank()) newStep.setSectionId(sectionId);
                    lesson.addStep(newStep);
                }
            }
            return lesson;
        }

        private String str(JsonObject o, String key)
        {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
        }
    }
}
