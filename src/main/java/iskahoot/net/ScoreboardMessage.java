package iskahoot.net;

import java.util.List;
import java.util.Map;

/** Mensagem enviada pelo servidor com o placar atualizado/resultado da ronda. */
public class ScoreboardMessage implements Message {
    public String gameCode;
    public int questionIndex;
    public String info;
    public Map<String,Integer> scoreboard;       // pontuacao acumulada
    public Map<String,Integer> roundPoints;      // pontos ganhos nesta ronda
    public List<String> ranking;                 // equipas ordenadas por pontuacao desc

    public ScoreboardMessage(String gameCode, int questionIndex, String info,
                             Map<String,Integer> scoreboard,
                             Map<String,Integer> roundPoints,
                             List<String> ranking) {
        this.gameCode = gameCode;
        this.questionIndex = questionIndex;
        this.info = info;
        this.scoreboard = scoreboard;
        this.roundPoints = roundPoints;
        this.ranking = ranking;
    }
}
