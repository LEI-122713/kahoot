package iskahoot.model;


// Representa uma pergunta do quiz.
// Contém o texto da pergunta, a lista de opções, o índice da resposta correta e os pontos atribuídos.

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    public String question;
    public int points;
    public int correct; // 0-based index
    public java.util.List<String> options;
}
