package iskahoot.io;

// Carrega as perguntas a partir de /resources (JSON) usando Gson para converter em QuestionsFile/Quiz.

import com.google.gson.Gson;
import iskahoot.model.QuestionsFile;
import iskahoot.model.Quiz;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QuestionLoader {
    public static QuestionsFile loadFromResource(String path){
        try {
            InputStream in = QuestionLoader.class.getResourceAsStream(path);
            if (in == null) throw new RuntimeException("Resource not found: " + path);
            try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(r, QuestionsFile.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load questions: " + e.getMessage(), e);
        }
    }

    /**
     * Devolve o quiz principal carregado do ficheiro.
     * Se existir lista de quizzes, usa o primeiro; caso contrário usa a lista simples de perguntas.
     */
    public static Quiz pickQuiz(QuestionsFile qf) {
        if (qf == null) throw new IllegalArgumentException("QuestionsFile null");
        if (qf.quizzes != null && !qf.quizzes.isEmpty()) {
            return qf.quizzes.get(0);
        }
        if (qf.questions != null && !qf.questions.isEmpty()) {
            return new Quiz("Default", qf.questions);
        }
        throw new IllegalStateException("No quizzes/questions found in file");
    }
}
