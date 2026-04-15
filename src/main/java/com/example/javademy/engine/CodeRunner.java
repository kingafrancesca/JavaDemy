package com.example.javademy.engine;
import javax.tools.*;
import java.io.*;
import java.nio.file.*;

/**
 * Kompiluje kod Java napisany przez ucznia.
 * Wykonanie skompilowanego kodu delegowane jest do {@link CodeGuard}.
 */
public class CodeRunner
{
    private static final String TEMP_DIR   = "temp/code";
    private static final String CLASS_NAME = "StudentSolution";

    private final CodeGuard codeGuard;

    /** Tworzy instancję CodeRunner i przygotowuje folder tymczasowy na pliki ucznia. */
    public CodeRunner()
    {
        this.codeGuard = new CodeGuard();
        try {
            Files.createDirectories(Paths.get(TEMP_DIR));
        }
        catch (IOException e) {
            throw new RuntimeException("Nie można utworzyć folderu tymczasowego", e);
        }
    }

    /**
     * Kompiluje podany kod Java i zwraca wynik kompilacji.
     * Przed kompilacją kod jest owijany w klasę StudentSolution, jeśli nie zawiera
     * własnej deklaracji klasy publicznej.
     *
     * @param code kod Java do skompilowania.
     * @return wynik kompilacji zawierający status i ewentualne błędy.
     */
    public CompilationResult compile(String code)
    {
        String wrappedCode = wrapCode(code);
        String sourceFile  = TEMP_DIR + "/" + CLASS_NAME + ".java";

        try {
            Files.writeString(Paths.get(sourceFile), wrappedCode);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) return new CompilationResult(false, "Kompilator Java niedostępny - uruchom aplikację przez JDK, nie JRE");

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null)) {
                Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(sourceFile);
                JavaCompiler.CompilationTask task = compiler.getTask(null, fm, diagnostics, null, null, units);
                boolean success = task.call();
                if (success) return new CompilationResult(true, null);

                StringBuilder errors = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics())
                    if (d.getKind() == Diagnostic.Kind.ERROR) errors.append("Linia ").append(d.getLineNumber()).append(": ").append(d.getMessage(null)).append("\n");
                return new CompilationResult(false, errors.toString());
            }
        }
        catch (IOException e) {
            return new CompilationResult(false, "Błąd zapisu pliku: " + e.getMessage());
        }
    }

    /**
     * Kompiluje kod ucznia, a następnie uruchamia go przez {@link CodeGuard}.
     * Jeśli kompilacja się nie powiedzie, zwraca błąd kompilacji bez uruchamiania.
     *
     * @param code kod Java do skompilowania i uruchomienia
     * @return wynik wykonania programu
     */
    public CodeGuard.ExecutionResult compileAndRun(String code)
    {
        CompilationResult compilation = compile(code);
        if (!compilation.success())
            return new CodeGuard.ExecutionResult(false, "", compilation.errorMessage());
        return codeGuard.execute(CLASS_NAME, new File(TEMP_DIR).getAbsolutePath());
    }

    /**
     * Owija kod w klasę StudentSolution tylko wtedy, gdy kod nie zawiera
     * własnej deklaracji klasy publicznej.
     * Jeśli kod zawiera deklarację {@code public class}, zmienia jej nazwę
     * na StudentSolution, żeby {@link CodeGuard} wiedział, co uruchomić.
     * Jeśli brak deklaracji klasy, owija kod w klasę i metodę main.
     *
     * @param code surowy kod ucznia.
     * @return kod gotowy do kompilacji z nazwą klasy StudentSolution.
     */
    private String wrapCode(String code)
    {
        if (code.contains("public class "))
        {
            return code.replaceFirst(
                    "public class (\\w+)",
                    "public class " + CLASS_NAME
            );
        }

        return "public class " + CLASS_NAME + " {\n" +
                "    public static void main(String[] args) {\n" +
                code + "\n" +
                "    }\n" +
                "}";
    }

    /**
     * Wynik kompilacji kodu ucznia.
     *
     * @param success      czy kompilacja zakończyła się sukcesem
     * @param errorMessage komunikat o błędzie lub null, jeśli kompilacja powiodła się
     */
    public record CompilationResult(boolean success, String errorMessage) {}
}
