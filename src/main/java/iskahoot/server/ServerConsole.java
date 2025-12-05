package iskahoot.server;

import java.util.Map;
import java.util.Scanner;

/**
 * TUI do servidor: cria jogos com códigos e lista estado/placar dos jogos ativos.
 */
public class ServerConsole implements Runnable {

    private final GameManager gm;

    public ServerConsole(GameManager gm) {
        this.gm = gm;
    }

    @Override
    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("[TUI] Comandos: new <equipas> <jogadores> <perguntas> | list | help");
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase();
                switch (cmd) {
                    case "new" -> handleNew(parts);
                    case "list" -> handleList();
                    case "help" -> printHelp();
                    default -> System.out.println("Comando desconhecido. Usa help.");
                }
            }
        }
    }

    private void handleNew(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Uso: new <nEquipas> <jogadoresPorEquipa> <nPerguntas>");
            return;
        }
        try {
            int teams = Integer.parseInt(parts[1]);
            int players = Integer.parseInt(parts[2]);
            int questions = Integer.parseInt(parts[3]);
            GameRoom room = gm.createGame(teams, players, questions);
            System.out.println("Jogo criado: código " + room.code + " (equipas=" + teams +
                    ", jogadores/equipa=" + players + ", perguntas=" + questions + ")");
        } catch (NumberFormatException e) {
            System.out.println("Parâmetros inválidos. Usa números inteiros.");
        }
    }

    private void handleList() {
        var infos = gm.describeGames();
        if (infos.isEmpty()) {
            System.out.println("Sem jogos ativos.");
            return;
        }
        infos.forEach(info -> System.out.println(" - " + info));
    }

    private void printHelp() {
        System.out.println("new <nEquipas> <jogadoresPorEquipa> <nPerguntas>  -> cria jogo e mostra código");
        System.out.println("list                                                -> lista jogos ativos");
    }
}
