package com.example.javademy.auth;
import com.example.javademy.model.Student;

/**
 * Przechowuje informacje o aktualnie zalogowanym użytkowniku.
 * Resetowana po wylogowaniu użytkownika.
 */
public class CurrentUser
{
    private static CurrentUser instance;
    private Student student;

    private CurrentUser() {}

    /**
     * Zwraca instancję CurrentUser.
     * Tworzy ją, jeśli jeszcze nie istnieje.
     *
     * @return instancja CurrentUser.
     */
    public static CurrentUser getInstance()
    {
        if (instance == null) instance = new CurrentUser();
        return instance;
    }

    /**
     * Ustawia zalogowanego użytkownika po pomyślnym logowaniu.
     *
     * @param student zalogowany użytkownik.
     */
    public void setStudent(Student student)
    {
        this.student = student;
    }

    /**
     * Zwraca aktualnie zalogowanego użytkownika.
     *
     * @return zalogowany użytkownik lub null, jeśli nikt nie jest zalogowany.
     */
    public Student getStudent()
    {
        return student;
    }

    /**
     * Wylogowuje aktualnego użytkownika.
     * Po wywołaniu tej metody getStudent() zwróci null.
     */
    public void logout()
    {
        this.student = null;
    }
}