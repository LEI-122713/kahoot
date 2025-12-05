package iskahoot.model;

import java.util.*;

/**
 * Estado local do quiz (modo offline): perguntas baralhadas, índice atual, placar por equipa.
 * Inclui registo opcional de acertos por pergunta para estatísticas simples.
 */

public class GameState {

    private final List<Question> questions;                 // perguntas do quiz (baralhadas)
    private int idx = 0;                                    // índice da pergunta atual (0..n-1)
    private final LinkedHashMap<String,Integer> scoreboard = new LinkedHashMap<>();

    // (opcional p/ estatísticas de ronda): questionIndex -> (teamId -> acertou?)
    private final Map<Integer, Map<String, Boolean>> answersByQuestion = new HashMap<>();

    public GameState(List<Question> questions, List<String> teamIds) {
        // cópia defensiva + baralhar (ordem aleatória em cada execução)
        this.questions = new ArrayList<>(Objects.requireNonNull(questions, "questions"));
        Collections.shuffle(this.questions, new Random());

        // inicializar placar (ordem das equipas preservada)
        Objects.requireNonNull(teamIds, "teamIds").forEach(t -> scoreboard.put(t, 0));
    }

    public GameState(Quiz quiz, List<String> teamIds) {
        this(quiz.safeQuestions(), teamIds);
    }

    /* ===== Progresso ===== */

    /** Há mais perguntas por responder? */

    public boolean hasNext() {
        return idx < questions.size();
    }

    /** Pergunta atual (lança exceção se o quiz já terminou). */

    public Question current() {
        if (!hasNext()) throw new IllegalStateException("No current question (quiz finished)");
        return questions.get(idx);
    }

    /** Avança para a próxima pergunta (ignora se já estiver no fim). */

    public void next() {
        if (hasNext()) idx++;
    }

    /** Nº total de perguntas. */

    public int total() {
        return questions.size();
    }

    /** Índice (0-based) da pergunta atual. */

    public int index() {
        return idx;
    }

    /* ===== Respostas & Pontuação ===== */

    /**
     * Regista a resposta da equipa. Soma pontos se correta e guarda resultado para estatísticas.
     * @param teamId equipa (ex.: "Team1")
     * @param option índice da opção escolhida (0..)
     * @return true se acertou, false caso contrário
     */
    public boolean submitAnswer(String teamId, int option) {
        if (!scoreboard.containsKey(teamId))
            throw new IllegalArgumentException("Unknown team: " + teamId);

        Question q = current(); // pode lançar se já terminou
        boolean correct = (option == q.correct);

        if (correct) {
            scoreboard.put(teamId, scoreboard.get(teamId) + q.points);
        }

        // registar estatística da pergunta atual
        answersByQuestion
                .computeIfAbsent(idx, k -> new HashMap<>())
                .put(teamId, correct);

        return correct;
    }

    /** Placar (cópia apenas-leitura). */

    public Map<String,Integer> getScoreboard() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(scoreboard));
    }

    /** (prep) Estatísticas de uma pergunta: teamId -> acertou? */

    public Map<String, Boolean> getAnswersFor(int questionIndex) {
        Map<String, Boolean> m = answersByQuestion.get(questionIndex);
        return (m == null) ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(m));
    }


    /* ===== Utilidades ===== */

    /** Reinicia o quiz: volta ao início, zera placar e estatísticas; volta a baralhar perguntas. */

    public void reset() {
        idx = 0;
        scoreboard.replaceAll((k, v) -> 0);
        answersByQuestion.clear();
        Collections.shuffle(this.questions, new Random());
    }
}
