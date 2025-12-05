package iskahoot.model;

import java.util.List;

// Representa o ficheiro JSON com as perguntas: aceita lista simples ou lista de quizzes nomeados.
public class QuestionsFile {
    public List<Question> questions; // formato simples
    public List<Quiz> quizzes;       // formato com vários quizzes, sugerido pela professora
}
