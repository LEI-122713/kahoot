package iskahoot.net;

/**
 * Enviada pelo servidor para indicar fim do jogo.
 */
public class GameOverMessage implements Message {
    public String gameCode;
    public String info;

    public GameOverMessage(String gameCode, String info) {
        this.gameCode = gameCode;
        this.info = info;
    }
}
