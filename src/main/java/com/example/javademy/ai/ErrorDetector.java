package com.example.javademy.ai;
import java.util.ArrayList;
import java.util.List;

/**
 * Analizuje błędy popełniane przez ucznia i przypisuje im tagi.
 * Tagi są używane przez ErrorAnalyzer do wykrywania wzorców błędów.
 * Każdy tag reprezentuje konkretny typ błędu w języku Java.
 */
public class ErrorDetector
{
    /** Tag dla błędów związanych z wartością null. */
    public static final String NULL_POINTER = "null-pointer";

    /** Tag dla błędów związanych z rzutowaniem typów. */
    public static final String WRONG_CAST = "wrong-cast";

    /** Tag dla błędów związanych z obsługą wyjątków. */
    public static final String EXCEPTION_HANDLING = "exception-handling";

    /** Tag dla błędów związanych z tablicami. */
    public static final String ARRAY_INDEX = "array-index";

    /** Tag dla błędów związanych z typami danych. */
    public static final String DATA_TYPE = "data-type";

    /** Tworzy instancję ErrorDetector. */
    public ErrorDetector() {}

    /**
     * Wykrywa tagi błędów na podstawie komunikatu o błędzie kompilacji lub wykonania.
     * Analizuje treść błędu i dopasowuje go do znanych wzorców.
     *
     * @param errorMessage komunikat o błędzie
     * @return lista tagów wykrytych błędów
     */
    public List<String> detectErrors(String errorMessage)
    {
        List<String> tags = new ArrayList<>();
        if (errorMessage == null || errorMessage.isEmpty()) return tags;

        String lower = errorMessage.toLowerCase();

        if (lower.contains("nullpointerexception") || lower.contains("null"))
        {
            tags.add(NULL_POINTER);
        }
        if (lower.contains("arrayindexoutofbounds") || lower.contains("stringindexoutofbounds")
                || lower.contains("index out of bounds") || lower.contains("index out of range"))
        {
            tags.add(ARRAY_INDEX);
        }
        if (lower.contains("classcastexception") || lower.contains("cannot cast"))
        {
            tags.add(WRONG_CAST);
        }
        if (lower.contains("incompatible types") || lower.contains("cannot convert"))
        {
            tags.add(DATA_TYPE);
        }
        if (lower.contains("exception") && !lower.contains("nullpointer")
                && !lower.contains("arrayindex") && !lower.contains("classcast"))
        {
            tags.add(EXCEPTION_HANDLING);
        }

        return tags;
    }

    /**
     * Wykrywa tagi błędów na podstawie tagów przypisanych do zadania,
     * gdy uczeń odpowie niepoprawnie.
     * Używane dla zadań quizowych, gdzie nie ma komunikatu o błędzie kompilacji.
     *
     * @param exerciseErrorTags tagi błędów przypisane do zadania
     * @return lista wykrytych tagów
     */
    public List<String> detectFromTags(String[] exerciseErrorTags) {
        List<String> tags = new ArrayList<>();
        if (exerciseErrorTags == null) return tags;
        for (String tag : exerciseErrorTags)
            if (tag != null && !tag.isEmpty()) tags.add(tag);
        return tags;
    }
}