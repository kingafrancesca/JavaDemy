package com.example.javademy.auth;
import com.example.javademy.model.Student;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Obsługuje rejestrację i logowanie użytkowników w aplikacji JavaDemy.
 * Hasła są przechowywane w formie zahashowanej algorytmem SHA-256.
 */
public class AccountService
{
    private final ProfileManager profileManager;

    /**
     * Tworzy instancję AccountService.
     *
     * @param profileManager menedżer profili użytkowników
     */
    public AccountService(ProfileManager profileManager)
    {
        this.profileManager = profileManager;
    }

    /**
     * Rejestruje nowego użytkownika.
     *
     * @param login wybrany login
     * @param password hasło w formie jawnej
     * @param displayName imię wyświetlane w aplikacji
     * @return true, jeśli rejestracja się powiodła, false, jeśli login zajęty
     */
    public boolean register(String login, String password, String displayName) {
        if (profileManager.studentExists(login)) return false;

        String passwordHash = hashPassword(password);
        Student student = new Student(login, passwordHash, displayName);
        profileManager.saveStudent(student);
        return true;
    }

    /**
     * Loguje użytkownika, sprawdzając login i hasło.
     * Przy udanym logowaniu aktualizuje serię dni nauki i zapisuje profil.
     *
     * @param login login użytkownika
     * @param password hasło w formie jawnej
     * @return obiekt Student, jeśli dane poprawne; null, jeśli błędne.
     */
    public Student login(String login, String password)
    {
        Student student = profileManager.loadStudent(login);
        if (student == null) return null;

        String passwordHash = hashPassword(password);
        if (!student.getPasswordHash().equals(passwordHash)) return null;

        student.updateLoginStreak();
        profileManager.saveStudent(student);

        return student;
    }

    /**
     * Sprawdza, czy podane hasło zgadza się z przechowanym hashem.
     *
     * @param storedHash zapisany hash SHA-256
     * @param inputPassword hasło w formie jawnej do sprawdzenia
     * @return true, jeśli hasło jest poprawne
     */
    public boolean verifyPassword(String storedHash, String inputPassword)
    {
        return storedHash.equals(hashPassword(inputPassword));
    }

    /**
     * Aktualizuje profil użytkownika - imię i/lub hasło.
     * Zmiany zapisywane są zarówno w obiekcie w pamięci, jak i w pliku JSON.
     *
     * @param student obiekt ucznia do aktualizacji (CurrentUser)
     * @param newDisplayName nowe imię (null lub puste = bez zmiany)
     * @param newPassword nowe hasło w formie jawnej (null lub puste = bez zmiany)
     */
    public void updateProfile(Student student, String newDisplayName, String newPassword) {
        if (newDisplayName != null && !newDisplayName.isBlank()) student.setDisplayName(newDisplayName.trim());
        if (newPassword != null && !newPassword.isBlank()) student.setPasswordHash(hashPassword(newPassword));
        profileManager.saveStudent(student);
    }

    /**
     * Hashuje hasło algorytmem SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Błąd hashowania hasła: algorytm SHA-256 niedostępny", e);
        }
    }
}