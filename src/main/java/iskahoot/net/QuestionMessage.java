package iskahoot.net;

import java.util.List;

/** Mensagem enviada pelo servidor para anunciar a pergunta da ronda atual. */
public class QuestionMessage implements Message {
    public String gameCode;
    public int questionIndex;
    public int totalQuestions;
    public String questionText;
    public List<String> options;
    public int points;
    public int seconds;

    public QuestionMessage(String gameCode, int questionIndex, int totalQuestions, String questionText,
                           List<String> options, int points, int seconds) {
        this.gameCode = gameCode;
        this.questionIndex = questionIndex;
        this.totalQuestions = totalQuestions;
        this.questionText = questionText;
        this.options = options;
        this.points = points;
        this.seconds = seconds;
    }
}
