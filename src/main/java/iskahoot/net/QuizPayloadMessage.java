package iskahoot.net;

import iskahoot.model.Quiz;

/** Mensagem enviada pelo servidor após join com o QUIZ completo para o cliente usar localmente. */
public class QuizPayloadMessage implements Message {
    public String gameCode;
    public Quiz quiz;

    public QuizPayloadMessage(String gameCode, Quiz quiz) {
        this.gameCode = gameCode;
        this.quiz = quiz;
    }
}
