package iskahoot.net;

/**
 * Enviada pelo cliente com a opção escolhida.
 */
public class AnswerMessage implements Message {
    public String gameCode;
    public String teamId;
    public String username;
    public int questionIndex;
    public int option; // 0-based

    public AnswerMessage(String gameCode, String teamId, String username, int questionIndex, int option) {
        this.gameCode = gameCode;
        this.teamId = teamId;
        this.username = username;
        this.questionIndex = questionIndex;
        this.option = option;
    }
}
