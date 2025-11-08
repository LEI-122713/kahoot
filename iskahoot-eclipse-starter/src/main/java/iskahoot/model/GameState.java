package iskahoot.model;

import java.util.*;

public class GameState {
  private final List<Question> questions;
  private int idx = 0;

  // placar simples (uma ou mais equipas se quiseres)
  private final Map<String,Integer> scoreboard = new LinkedHashMap<>();

  public GameState(List<Question> questions, List<String> teamIds) {
    this.questions = new ArrayList<>(questions);
    for (String t : teamIds) scoreboard.put(t, 0);
  }

  public boolean hasNext() { return idx < questions.size(); }
  public Question current() { return questions.get(idx); }
  public void next() { idx++; }

  public void submitAnswer(String teamId, int option) {
    Question q = questions.get(idx);
    if (option == q.correct) {
      scoreboard.put(teamId, scoreboard.get(teamId) + q.points);
    }
  }

  public Map<String,Integer> getScoreboard() {
    return new LinkedHashMap<>(scoreboard);
  }
}
