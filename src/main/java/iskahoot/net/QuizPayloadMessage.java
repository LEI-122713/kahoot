package iskahoot.net;

import iskahoot.model.Quiz;

/**
 * Enviada pelo servidor logo apos join bem-sucedido com o quiz completo.
 * Permite ao cliente ter as perguntas localmente (modelar e simples).
 */
public class QuizPayloadMessage implements Message {
    public String gameCode;
    public Quiz quiz;

    public QuizPayloadMessage(String gameCode, Quiz quiz) {
        this.gameCode = gameCode;
        this.quiz = quiz;
    }
}
