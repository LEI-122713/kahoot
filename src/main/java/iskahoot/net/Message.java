package iskahoot.net;

import java.io.Serializable;

/**
 * Mensagem base para comunicação cliente-servidor.
 * Todas as mensagens enviadas pela rede implementam Serializable.
 */
public interface Message extends Serializable {
}
