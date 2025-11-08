package iskahoot.model;

import java.util.*;

/**
 * GameState (versão da entrega intermédia)
 * - Guarda as perguntas (já carregadas do JSON)
 * - Controla o índice da pergunta atual
 * - Mantém um placar por equipa
 * Sem rede e sem concorrência nesta fase.
 */
public class GameState {
  private final List<Question> questions;           // perguntas do quiz
  private int idx = 0;                              // pergunta atual (0..n-1)
  private final LinkedHashMap<String,Integer> scoreboard = new LinkedHashMap<>();

  public GameState(List<Question> questions, List<String> teamIds) {
    // cópia defensiva para não mexer na lista original
    this.questions = new ArrayList<>(questions);
    // cria as equipas no placar a zero (ordem preservada)
    for (String t : teamIds) scoreboard.put(t, 0);
  }

  /* ====== Progresso ====== */

  /** Há mais perguntas por responder? */
  public boolean hasNext() { return idx < questions.size(); }

  /** Pergunta atual (sem avançar). */
  public Question current() { return questions.get(idx); }

  /** Avança para a próxima pergunta. */
  public void next() { idx++; }

  /** Nº total de perguntas. */
  public int total() { return questions.size(); }

  /** Índice (0-based) da pergunta atual. */
  public int index() { return idx; }

  /* ====== Respostas & Pontuação ====== */

  /**
   * Regista a resposta da equipa. Se estiver certa, soma os pontos da pergunta.
   * @param teamId  ex.: "Team1"
   * @param option  índice da opção escolhida (0..)
   * @return true se acertou, false se errou
   */
  public boolean submitAnswer(String teamId, int option) {
    if (!scoreboard.containsKey(teamId)) return false; // equipa inválida
    Question q = current();
    boolean correct = (option == q.correct);
    if (correct) scoreboard.put(teamId, scoreboard.get(teamId) + q.points);
    return correct;
  }

  /** Placar (cópia somente-leitura para mostrar na GUI). */
  public Map<String,Integer> getScoreboard() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(scoreboard));
  }

  /* ====== Utilidades opcionais ====== */

  /** Reinicia o quiz (volta ao início e zera o placar). */
  public void reset() {
    idx = 0;
    for (var k : scoreboard.keySet()) scoreboard.put(k, 0);
  }
}
