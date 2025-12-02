package iskahoot.net;

/**
 * Enviada pelo servidor em caso de erro simples.
 */
public class ErrorMessage implements Message {
    public String info;

    public ErrorMessage(String info) {
        this.info = info;
    }
}
