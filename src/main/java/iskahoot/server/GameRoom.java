package iskahoot.server;

import iskahoot.net.JoinResponse;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Representa um jogo criado no servidor.
 * Mantém limites de equipas/jogadores e regista quem entrou.
 * Nesta fase apenas gere validações de entrada.
 */
public class GameRoom {

    public final String code;
    private final int maxTeams;
    private final int playersPerTeam;
    private final int numQuestions;

    // ordem de criação preservada
    private final LinkedHashMap<String, Set<String>> teams = new LinkedHashMap<>();

    public GameRoom(String code, int maxTeams, int playersPerTeam, int numQuestions) {
        this.code = code;
        this.maxTeams = maxTeams;
        this.playersPerTeam = playersPerTeam;
        this.numQuestions = numQuestions;
    }

    public synchronized JoinResponse tryJoin(String teamId, String username) {
        if (isFull()) {
            return new JoinResponse(false, "Jogo cheio");
        }

        Set<String> players = teams.get(teamId);
        if (players == null) {
            if (teams.size() >= maxTeams) {
                return new JoinResponse(false, "Número de equipas excedido");
            }
            players = new LinkedHashSet<>();
            teams.put(teamId, players);
        }

        if (players.size() >= playersPerTeam) {
            return new JoinResponse(false, "Equipa cheia");
        }

        players.add(username);
        return new JoinResponse(true, "Bem-vindo " + username + " à equipa " + teamId + " (jogo " + code + ")");
    }

    public synchronized void removeUser(String username) {
        teams.values().forEach(set -> set.remove(username));
    }

    public synchronized boolean isFull() {
        return totalPlayers() >= maxTeams * playersPerTeam;
    }

    public synchronized int totalPlayers() {
        return teams.values().stream().mapToInt(Set::size).sum();
    }

    public int maxTeams() {
        return maxTeams;
    }

    public int playersPerTeam() {
        return playersPerTeam;
    }

    public int numQuestions() {
        return numQuestions;
    }

    public synchronized int expectedTotalPlayers() {
        return maxTeams * playersPerTeam;
    }

    public synchronized Map<String, Set<String>> snapshotTeams() {
        LinkedHashMap<String, Set<String>> copy = new LinkedHashMap<>();
        teams.forEach((k, v) -> copy.put(k, new LinkedHashSet<>(v)));
        return copy;
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(code)
                .append(" [equipas ").append(teams.size()).append("/").append(maxTeams)
                .append(", jogadores ").append(totalPlayers()).append("/").append(maxTeams * playersPerTeam)
                .append(", perguntas ").append(numQuestions).append("]");
        if (!teams.isEmpty()) {
            sb.append(" -> ");
            teams.forEach((team, players) -> sb.append(team).append("=").append(players.size()).append(" "));
        }
        return sb.toString().trim();
    }
}
