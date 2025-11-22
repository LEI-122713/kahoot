package iskahoot.net;

/**
 * Mensagem enviada pelo cliente para pedir entrada num jogo.
 */
public class JoinMessage implements Message {
    public String gameCode;  // código do jogo (ex.: "ABCD")
    public String teamId;    // equipa (ex.: "Team1")
    public String username;  // nome do jogador

    public JoinMessage(String gameCode, String teamId, String username) {
        this.gameCode = gameCode;
        this.teamId = teamId;
        this.username = username;
    }
}
