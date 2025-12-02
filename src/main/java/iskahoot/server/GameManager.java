package iskahoot.server;

import iskahoot.net.JoinMessage;
import iskahoot.net.JoinResponse;

import java.security.SecureRandom;
import java.util.*;

/**
 * Guarda os jogos ativos e valida pedidos de join.
 * Implementação simples e sincronizada (fase 4).
 */
public class GameManager {

    private final Map<String, GameRoom> games = new HashMap<>();
    private final Set<String> usernames = new HashSet<>();
    private final SecureRandom random = new SecureRandom();
    private final Map<String, GameSession> sessions = new HashMap<>();

    public synchronized GameRoom createGame(int numTeams, int playersPerTeam, int numQuestions) {
        String code = generateCode();
        GameRoom room = new GameRoom(code, numTeams, playersPerTeam, numQuestions);
        games.put(code, room);
        return room;
    }

    public synchronized JoinResponse handleJoin(JoinMessage join) {
        if (join.username == null || join.username.isBlank()) {
            return new JoinResponse(false, "Username em branco");
        }
        if (usernames.contains(join.username)) {
            return new JoinResponse(false, "Username já em uso");
        }
        GameRoom room = games.get(join.gameCode);
        if (room == null) {
            return new JoinResponse(false, "Jogo inexistente");
        }

        JoinResponse resp = room.tryJoin(join.teamId, join.username);
        if (resp.ok) {
            usernames.add(join.username);
        }
        return resp;
    }

    public synchronized void disconnectUser(String gameCode, String username) {
        if (username == null) return;
        usernames.remove(username);
        GameRoom room = games.get(gameCode);
        if (room != null) {
            room.removeUser(username);
        }
    }

    public synchronized GameRoom getRoom(String gameCode) {
        return games.get(gameCode);
    }

    public synchronized GameSession getOrCreateSession(String code, GameRoom room, iskahoot.model.Quiz quiz) {
        return sessions.computeIfAbsent(code, k -> new GameSession(code, quiz, room, this));
    }

    public synchronized Map<String, GameRoom> snapshotGames() {
        return new LinkedHashMap<>(games);
    }

    public synchronized void endGame(String code) {
        sessions.remove(code);
        games.remove(code);
    }

    private String generateCode() {
        String code;
        do {
            code = randomCode(4);
        } while (games.containsKey(code));
        return code;
    }

    private String randomCode(int len) {
        final String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }
}
