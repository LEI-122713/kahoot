package iskahoot.model;

import java.util.List;

// Representa o ficheiro JSON com as perguntas do jogo.
// Suporta dois formatos:
// 1) { "questions": [ ... ] }
// 2) { "quizzes": [ { "name": "...", "questions": [ ... ] }, ... ] }
public class QuestionsFile {
    public List<Question> questions; // formato simples
    public List<Quiz> quizzes;       // formato com vários quizzes
}
