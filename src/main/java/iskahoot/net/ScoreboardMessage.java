package iskahoot.net;

import java.util.Map;

/**
 * Enviada pelo servidor com o placar atualizado/resultado da ronda.
 */
public class ScoreboardMessage implements Message {
    public String gameCode;
    public int questionIndex;
    public String info;
    public Map<String,Integer> scoreboard;

    public ScoreboardMessage(String gameCode, int questionIndex, String info, Map<String,Integer> scoreboard) {
        this.gameCode = gameCode;
        this.questionIndex = questionIndex;
        this.info = info;
        this.scoreboard = scoreboard;
    }
}
