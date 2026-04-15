package com.example.javademy.auth;
import com.example.javademy.model.Student;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

/**
 * Zarządza zapisem i odczytem profili użytkowników na dysku.
 * Każdy użytkownik przechowywany jest w osobnym pliku JSON w folderze "data/users".
 */
public class ProfileManager
{
    private static final String USERS_DIR = "data/users";
    private final Gson gson;

    /** Tworzy instancję ProfileManager i inicjalizuje folder na profile użytkowników. */
    public ProfileManager()
    {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists()
    {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można utworzyć folderu na profile użytkowników", e);
        }
    }

    /**
     * Zapisuje profil użytkownika do pliku JSON.
     * Plik nazwany jest loginem użytkownika.
     *
     * @param student użytkownik do zapisania
     */
    public void saveStudent(Student student)
    {
        String filePath = USERS_DIR + "/" + student.getLogin() + ".json";
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(student, writer);
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można zapisać profilu użytkownika: " + student.getLogin(), e);
        }
    }

    /**
     * Wczytuje profil użytkownika z pliku JSON
     *
     * @param login login użytkownika do wczytania
     * @return obiekt Student lub null, jeśli profil nie istnieje
     */
    public Student loadStudent(String login) {
        String filePath = USERS_DIR + "/" + login + ".json";
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, Student.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można wczytać profilu użytkownika: " + login, e);
        }
    }

    /**
     * Sprawdza, czy użytkownik o podanym loginie już istnieje
     *
     * @param login login do sprawdzenia
     * @return true, jeśli użytkownik istnieje
     */
    public boolean studentExists(String login) {
        String filePath = USERS_DIR + "/" + login + ".json";
        return new File(filePath).exists();
    }
}