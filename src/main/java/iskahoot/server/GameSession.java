package iskahoot.server;

import iskahoot.concurrent.ModifiedCountdownLatch;
import iskahoot.concurrent.TeamBarrier;
import iskahoot.model.Question;
import iskahoot.model.Quiz;
import iskahoot.net.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Estado partilhado de um jogo (código específico).
 * Gere perguntas, placar global e coordenação de respostas entre clientes.
 */
public class GameSession {
    private final String code;
    private final Quiz quiz;
    private final GameRoom roomInfo;
    private final GameManager gm;

    private final List<Question> questions;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    // placar global: equipa -> pontos
    private final Map<String, Integer> scoreboard = new ConcurrentHashMap<>();

    // registo de utilizadores -> equipa
    private final Map<String, String> userTeam = new ConcurrentHashMap<>();

    // clientes ligados (para broadcast)
    private final Set<ClientEndpoint> clients = Collections.synchronizedSet(new LinkedHashSet<>());

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private RoundState roundState;

    public GameSession(String code, Quiz quiz, GameRoom roomInfo, GameManager gm) {
        this.code = code;
        this.quiz = quiz;
        this.roomInfo = roomInfo;
        this.gm = gm;
        List<Question> tmp = new ArrayList<>(quiz.questions);
        Collections.shuffle(tmp, new Random());
        int limit = Math.min(roomInfo.numQuestions(), tmp.size());
        this.questions = new ArrayList<>(tmp.subList(0, limit));
        roomInfo.snapshotTeams().forEach((team, players) -> scoreboard.put(team, 0));
    }

    public String code() {
        return code;
    }

    public void addClient(ClientEndpoint ce, String teamId, String username) {
        clients.add(ce);
        userTeam.put(username, teamId);
        if (!started.get() && allPlayersConnected()) {
            startGameLoop();
        } else if (started.get() && !finished.get()) {
            sendCurrentQuestionTo(ce);
        }
    }

    public void removeClient(ClientEndpoint ce, String username) {
        clients.remove(ce);
        userTeam.remove(username);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public synchronized void sendCurrentQuestionTo(ClientEndpoint ce) {
        int idx = currentIndex.get();
        if (idx >= questions.size()) return;
        Question q = questions.get(idx);
        QuestionMessage qm = new QuestionMessage(code, idx, questions.size(), q.question, q.options, q.points, 30);
        ce.send(qm);
    }

    /**
     * Processa uma resposta; se todas as equipas estiverem prontas ou timeout expirou, avança ronda.
     */
    public void handleAnswer(AnswerMessage ans) {
        if (!started.get() || finished.get()) return;
        int idx = currentIndex.get();
        if (idx >= questions.size()) return;
        if (ans.questionIndex != idx) return; // resposta fora de sincronia
        RoundState rs = roundState;
        if (rs == null) return;
        rs.processAnswer(ans);
    }

    private void broadcast(Message m) {
        synchronized (clients) {
            for (ClientEndpoint ce : clients) {
                ce.send(m);
            }
        }
    }

    private boolean allPlayersConnected() {
        return roomInfo.expectedTotalPlayers() > 0 && roomInfo.totalPlayers() >= roomInfo.expectedTotalPlayers();
    }

    private void startGameLoop() {
        if (!started.compareAndSet(false, true)) return;
        new Thread(this::gameLoop, "game-session-" + code).start();
    }

    private void gameLoop() {
        for (int i = 0; i < questions.size(); i++) {
            currentIndex.set(i);
            Question q = questions.get(i);
            boolean isTeamQuestion = (i % 2 == 1);
            roundState = new RoundState(q, i, isTeamQuestion);
            broadcast(new QuestionMessage(code, i, questions.size(), q.question, q.options, q.points, 30));
            roundState.awaitRoundEnd();
            broadcast(roundState.buildScoreboardMessage(code));
            if (finished.get()) break;
        }
        finished.set(true);
        broadcast(new GameOverMessage(code, "Fim do jogo"));
        if (gm != null) {
            gm.endGame(code);
        }
    }

    /**
     * Estado de uma ronda/pergunta.
     */
    private class RoundState {
        private final Question question;
        private final int idx;
        private final boolean teamQuestion;
        private final ModifiedCountdownLatch latch;
        private final Map<String, TeamBarrier> teamBarriers = new HashMap<>();
        private final Map<String, List<AnswerMessage>> answersByTeam = new HashMap<>();
        private final Set<String> answeredUsers = new HashSet<>();
        private final AtomicBoolean ended = new AtomicBoolean(false);
        private final Map<String,Integer> roundPoints = new HashMap<>();
        private final long deadline = System.currentTimeMillis() + 30_000;
        private final AtomicInteger teamsFinished = new AtomicInteger(0);
        private final int totalTeams;

        RoundState(Question q, int idx, boolean teamQuestion) {
            this.question = q;
            this.idx = idx;
            this.teamQuestion = teamQuestion;
            this.latch = new ModifiedCountdownLatch(2, 2, 30, expectedPlayersIndividual());
            Map<String, Set<String>> snapshot = roomInfo.snapshotTeams();
            this.totalTeams = snapshot.size();
            snapshot.forEach((team, users) -> {
                teamBarriers.put(team, new TeamBarrier(roomInfo.playersPerTeam(), 30, () -> onTeamBarrierRelease(team)));
                answersByTeam.put(team, new ArrayList<>());
                roundPoints.put(team, 0);
            });
        }

        private int expectedPlayersIndividual() {
            return Math.max(1, roomInfo.totalPlayers());
        }

        void processAnswer(AnswerMessage ans) {
            synchronized (this) {
                if (ended.get()) return;
                if (!answeredUsers.add(ans.username)) return; // já respondeu
            }
            if (teamQuestion) {
                handleTeamAnswer(ans);
            } else {
                handleIndividualAnswer(ans);
            }
            checkEndCondition();
        }

        private void handleIndividualAnswer(AnswerMessage ans) {
            boolean correct = (ans.option == question.correct);
            int factor = latch.countdown();
            if (correct) {
                int gained = question.points * factor;
                scoreboard.merge(ans.teamId, gained, Integer::sum);
                roundPoints.merge(ans.teamId, gained, Integer::sum);
            }
        }

        private void handleTeamAnswer(AnswerMessage ans) {
            List<AnswerMessage> teamList = answersByTeam.get(ans.teamId);
            if (teamList == null) return;
            synchronized (teamList) {
                teamList.add(ans);
            }
            try {
                teamBarriers.get(ans.teamId).await();
            } catch (InterruptedException ignored) {
            }
        }

        private void evaluateTeam(String teamId) {
            List<AnswerMessage> teamList = answersByTeam.get(teamId);
            if (teamList == null || teamList.isEmpty()) return;
            boolean allCorrect = teamList.size() >= roomInfo.playersPerTeam();
            int best = 0;
            for (AnswerMessage am : teamList) {
                boolean correct = (am.option == question.correct);
                if (!correct) allCorrect = false;
                if (correct) best = Math.max(best, question.points);
            }
            int gained = allCorrect ? question.points * 2 : best;
            scoreboard.merge(teamId, gained, Integer::sum);
            roundPoints.merge(teamId, gained, Integer::sum);
        }

        void awaitRoundEnd() {
            if (teamQuestion) {
                waitTeamsOrTimeout();
            } else {
                waitIndividualsOrTimeout();
            }
        }

        private void timeout() {
            latch.expire();
            teamBarriers.values().forEach(TeamBarrier::release);
            // para equipas, avaliar o que houver
            answersByTeam.keySet().forEach(this::evaluateTeam);
            ended.set(true);
        }

        private void waitIndividualsOrTimeout() {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
            if (!latch.timedOut()) {
                ended.set(true);
            } else {
                timeout();
            }
        }

        private void waitTeamsOrTimeout() {
            while (true) {
                if (ended.get()) break;
                if (System.currentTimeMillis() >= deadline) {
                    timeout();
                    break;
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }

        private void checkEndCondition() {
            if (teamQuestion) {
                if (teamsFinished.get() >= totalTeams) {
                    ended.set(true);
                }
            } else {
                if (answeredUsers.size() >= expectedPlayersIndividual()) {
                    ended.set(true);
                }
            }
        }

        private void onTeamBarrierRelease(String teamId) {
            evaluateTeam(teamId);
            int done = teamsFinished.incrementAndGet();
            if (done >= totalTeams) {
                ended.set(true);
            }
        }

        ScoreboardMessage buildScoreboardMessage(String gameCode) {
            List<String> ranking = scoreboard.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();
            return new ScoreboardMessage(gameCode, idx, "Fim da ronda",
                    new HashMap<>(scoreboard),
                    new HashMap<>(roundPoints),
                    ranking);
        }
    }

    /**
     * Guardar os streams do cliente para broadcast.
     */
    public static class ClientEndpoint {
        private final ObjectOutputStream out;

        public ClientEndpoint(ObjectOutputStream out) {
            this.out = out;
        }

        public void send(Message m) {
            try {
                out.writeObject(m);
                out.flush();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Coordenação por pergunta (por jogo+índice).
     */
}
