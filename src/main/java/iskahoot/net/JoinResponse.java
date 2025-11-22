package iskahoot.net;

/**
 * Resposta do servidor a um pedido de Join.
 */
public class JoinResponse implements Message {
    public boolean ok;   // true se aceitou, false se recusou
    public String info;  // mensagem de explicação

    public JoinResponse(boolean ok, String info) {
        this.ok = ok;
        this.info = info;
    }
}
