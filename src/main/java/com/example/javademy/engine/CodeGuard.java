package com.example.javademy.engine;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * Izoluje wykonanie kodu ucznia, uruchamiając go w osobnym procesie.
 * Zapobiega wykonywaniu niebezpiecznych operacji i blokuje
 * nieskończone pętle przez limit czasu wykonania.
 */
public class CodeGuard
{
    private static final int TIMEOUT_SECONDS = 5;

    /**
     * Uruchamia skompilowany plik klasy w osobnym procesie.
     * Proces jest automatycznie zatrzymywany po przekroczeniu limitu czasu.
     *
     * @param className nazwa klasy do uruchomienia.
     * @param classDir folder zawierający skompilowany plik klasy.
     * @return wynik wykonania programu - output lub komunikat o błędzie.
     */
    public ExecutionResult execute(String className, String classDir) {
        cleanOutputFolder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", classDir, className);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    return output.toString();
                }
            });

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                executor.shutdownNow();
                return new ExecutionResult(false, "",
                        "Przekroczono limit czasu wykonania (" + TIMEOUT_SECONDS + "s). Sprawdź czy program nie zawiera nieskończonej pętli.");
            }

            String output = future.get();
            executor.shutdown();
            int exitCode = process.exitValue();

            if (exitCode != 0) return new ExecutionResult(false, output, "Program zakończył się błędem.");
            return new ExecutionResult(true, output, null);

        }
        catch (Exception e) {
            return new ExecutionResult(false, "", "Błąd uruchomienia: " + e.getMessage());
        }
    }

    private void cleanOutputFolder() {
        File outputDir = new File("data/files/output");
        if (!outputDir.exists() || !outputDir.isDirectory()) return;
        File[] files = outputDir.listFiles();
        if (files == null) return;
        for (File f : files)
        {
            if (!f.getName().startsWith("."))
                try {
                    Files.deleteIfExists(f.toPath());
                }
            catch (IOException ignored) {}
        }
    }

    /** Tworzy instancję CodeGuard. */
    public CodeGuard() {}

    /**
     * Przechowuje wynik wykonania kodu ucznia.
     *
     * @param success czy wykonanie zakończyło się sukcesem
     * @param output output programu
     * @param errorMessage komunikat błędu lub null
     */
    public record ExecutionResult(boolean success, String output, String errorMessage) {}
}