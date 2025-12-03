package iskahoot.client;

import iskahoot.net.*;
import iskahoot.model.Quiz;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Janela simples ligada ao servidor.
 * Recebe QuestionMessage/ScoreboardMessage/GameOverMessage e permite enviar AnswerMessage.
 */
public class NetworkKahootWindow extends JFrame implements AutoCloseable {

    private final JLabel lblTitle = new JLabel("IsKahoot - Cliente (Rede)");
    private final JLabel lblQuestion = new JLabel("Pergunta");
    private final OptionsListModel optionsModel = new OptionsListModel();
    private final JList<String> listOptions = new JList<>(optionsModel);
    private final JButton btnAnswer = new JButton("Responder");
    private final JLabel lblInfo = new JLabel("Placar: --");
    private final javax.swing.table.DefaultTableModel scoreboardModel =
            new javax.swing.table.DefaultTableModel(new Object[]{"Team", "Score", "Last round"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
    private final JTable tableScoreboard = new JTable(scoreboardModel);
    private final JLabel lblTeam = new JLabel("Equipa: --");
    private final JLabel lblTimer = new JLabel("Tempo: --s", SwingConstants.RIGHT);

    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final String gameCode;
    private final String teamId;
    private final String username;
    private final Quiz quiz;

    private Thread readerThread;
    private javax.swing.Timer swingTimer;

    public NetworkKahootWindow(NetGuiClient conn, String gameCode, String teamId, String username) {
        super("IsKahoot - Cliente (Rede)");
        this.out = conn.out();
        this.in = conn.in();
        this.gameCode = gameCode;
        this.teamId = teamId;
        this.username = username;
        this.quiz = conn.quiz();

        initUI();
        startReader();
    }

    private void initUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        JPanel north = new JPanel(new BorderLayout());
        north.add(lblTitle, BorderLayout.WEST);
        north.add(lblTeam, BorderLayout.CENTER);
        north.add(lblTimer, BorderLayout.EAST);
        String quizInfo = (quiz != null) ? " | Quiz: " + quiz.name + " (" + quiz.questions.size() + " perguntas)" : "";
        lblTeam.setText("Equipa: " + teamId + " | Utilizador: " + username + quizInfo);
        add(north, BorderLayout.NORTH);

        lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(lblQuestion, BorderLayout.NORTH);
        listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(new JScrollPane(listOptions), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        tableScoreboard.setFillsViewportHeight(true);
        tableScoreboard.setRowSelectionAllowed(false);
        JScrollPane scorePane = new JScrollPane(tableScoreboard);
        scorePane.setPreferredSize(new Dimension(220, 160));
        add(scorePane, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.add(lblInfo, BorderLayout.NORTH);
        south.add(btnAnswer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        btnAnswer.addActionListener(e -> sendAnswer());

        setSize(680, 460);
        setLocationRelativeTo(null);
    }

    private void startReader() {
        readerThread = new Thread(() -> {
            try {
                while (true) {
                    Object recv = in.readObject();
                    if (recv instanceof QuestionMessage q) {
                        SwingUtilities.invokeLater(() -> showQuestion(q));
                        startTimer(q.seconds);
                    } else if (recv instanceof ScoreboardMessage s) {
                        SwingUtilities.invokeLater(() -> showScoreboard(s));
                        stopTimer();
                    } else if (recv instanceof GameOverMessage g) {
                        SwingUtilities.invokeLater(() -> showGameOver(g));
                        break;
                    } else if (recv instanceof ErrorMessage e) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, e.info, "Erro", JOptionPane.ERROR_MESSAGE));
                    } else {
                        // desconhecido
                    }
                }
            } catch (Exception ex) {
                // ligacao terminou
            } finally {
                SwingUtilities.invokeLater(this::disableUI);
                stopTimer();
            }
        }, "net-reader");
        readerThread.start();
    }

    private void showQuestion(QuestionMessage q) {
        lblQuestion.setText("<html>" + (q.questionIndex + 1) + "/" + q.totalQuestions + ": " + q.questionText + " (" + q.points + " pts)</html>");
        optionsModel.setOptions(q.options);
        listOptions.clearSelection();
        listOptions.setEnabled(true);
        btnAnswer.setEnabled(true);
        btnAnswer.putClientProperty("questionIndex", q.questionIndex);
        lblInfo.setText("Placar: --");
    }

    private void showScoreboard(ScoreboardMessage s) {
        lblInfo.setText("Placar atualizado | " + s.info);
        updateScoreboardTable(s);
    }

    private String formatScoreboard(Map<String, Integer> sb) {
        if (sb == null || sb.isEmpty()) return "--";
        StringBuilder b = new StringBuilder();
        sb.forEach((k, v) -> b.append(k).append(": ").append(v).append("  "));
        return b.toString();
    }

    private void updateScoreboardTable(ScoreboardMessage s) {
        // limpar
        scoreboardModel.setRowCount(0);
        if (s == null || s.scoreboard == null) return;

        Map<String,Integer> round = (s.roundPoints != null) ? s.roundPoints : Map.of();
        List<String> order = new ArrayList<>();
        if (s.ranking != null) order.addAll(s.ranking);
        // garantir que todas as equipas do placar entram, mesmo que não tenham pontuado
        s.scoreboard.keySet().forEach(team -> { if (!order.contains(team)) order.add(team); });

        for (String team : order) {
            int total = s.scoreboard.getOrDefault(team, 0);
            int delta = round.getOrDefault(team, 0);
            scoreboardModel.addRow(new Object[]{team, total, delta});
        }
        // opcional: destacar a linha da equipa atual
        int myIndex = order.indexOf(teamId);
        if (myIndex >= 0) {
            tableScoreboard.setRowSelectionInterval(myIndex, myIndex);
        } else {
            tableScoreboard.clearSelection();
        }
    }

    private void showGameOver(GameOverMessage g) {
        disableUI();
        stopTimer();
        JOptionPane.showMessageDialog(this, "Fim do jogo: " + g.info, "Fim", JOptionPane.INFORMATION_MESSAGE);
    }

    private void disableUI() {
        listOptions.setEnabled(false);
        btnAnswer.setEnabled(false);
    }

    private void sendAnswer() {
        int sel = listOptions.getSelectedIndex();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Escolhe uma opcao primeiro.");
            return;
        }
        Object qiObj = btnAnswer.getClientProperty("questionIndex");
        int qIndex = (qiObj instanceof Integer) ? (Integer) qiObj : 0;
        try {
            AnswerMessage ans = new AnswerMessage(gameCode, teamId, username, qIndex, sel);
            out.writeObject(ans);
            out.flush();
            btnAnswer.setEnabled(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Falha ao enviar resposta: " + e.getMessage());
        }
    }

    // Timer visual (decrescente)
    private void startTimer(int seconds) {
        stopTimer();
        lblTimer.setText("Tempo: " + seconds + "s");
        final int[] remaining = {seconds};
        swingTimer = new javax.swing.Timer(1000, ev -> {
            remaining[0]--;
            int value = Math.max(0, remaining[0]);
            lblTimer.setText("Tempo: " + value + "s");
            if (value <= 0) {
                stopTimer();
                listOptions.setEnabled(false);
                btnAnswer.setEnabled(false);
                lblInfo.setText("Placar: tempo esgotado nesta ronda");
                Toolkit.getDefaultToolkit().beep();
            }
        });
        swingTimer.setInitialDelay(1000);
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    @Override
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        if (readerThread != null) readerThread.interrupt();
    }
}
