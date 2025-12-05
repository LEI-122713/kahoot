package iskahoot.client;

import iskahoot.model.Question;
import iskahoot.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Janela principal do modo offline (GameState local): mostra perguntas/opções e placar. CLASSE para a entrega intermédia
 */
public class KahootClientWindow extends JFrame {

    // tempo maximo (segundos) por pergunta
    private static final int QUESTION_TIME_SEC = 30;

    // Componentes visuais da janela
    private final JLabel lblTitle = new JLabel("IsKahoot - Cliente (Entrega Intermedia)");
    private final JLabel lblTimer = new JLabel("Tempo: --s", SwingConstants.RIGHT);

    private final JLabel lblQuestion = new JLabel("Pergunta");
    private final JList<String> listOptions = new JList<>();
    private final OptionsListModel optionsModel = new OptionsListModel();
    private final JButton btnAnswer = new JButton("Responder");
    private final JLabel lblInfo = new JLabel("Placar: --");
    private final JLabel lblTeam = new JLabel("A jogar como: --");

    // Equipa atual (passada pelo main)
    private final String myTeam;

    // Estado do jogo (ligado ao modelo)
    private final GameState gs;

    // Controlo do temporizador
    private javax.swing.Timer swingTimer;
    private int secondsLeft = QUESTION_TIME_SEC;

    // Indica se o jogo terminou
    private boolean gameOver = false;
    private boolean questionActive = false;

    public KahootClientWindow(GameState gs, String myTeam) {
        super("IsKahoot - Cliente");
        this.gs = gs;
        this.myTeam = myTeam;
        initUI();
        showQuestion();
        refreshScoreboard();
    }

    private void initUI() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        lblTimer.setFont(lblTitle.getFont().deriveFont(Font.PLAIN, 14f));
        JPanel header = new JPanel(new BorderLayout());
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblTimer, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(lblQuestion, BorderLayout.NORTH);

        listOptions.setModel(optionsModel);
        listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(new JScrollPane(listOptions), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        lblTeam.setText("A jogar como: " + myTeam);
        south.add(lblTeam, BorderLayout.NORTH);
        south.add(lblInfo, BorderLayout.CENTER);
        south.add(btnAnswer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        btnAnswer.addActionListener(e -> onAnswer());

        setSize(680, 460);
        setLocationRelativeTo(null);
    }

    private void showQuestion() {

        stopTimer(); // parar qualquer timer anterior

        // se o jogo acabou ou já não há perguntas, termina
        if (gameOver || !gs.hasNext()) {
            endUI();
            return;
        }

        Question q = gs.current();
        lblQuestion.setText("<html>" + q.question + " (" + q.points + " pts)</html>");
        optionsModel.setOptions(q.options);
        listOptions.clearSelection();
        listOptions.setEnabled(true);
        btnAnswer.setEnabled(true);
        questionActive = true;

        startTimer(); // iniciar contagem decrescente
    }

    private void onAnswer() {

        if (gameOver || !gs.hasNext() || !questionActive) return;

        if (secondsLeft <= 0) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        int sel = listOptions.getSelectedIndex();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Escolhe uma opcao primeiro.");
            return;
        }

        questionActive = false;
        stopTimer();
        boolean correct = gs.submitAnswer(myTeam, sel);
        JOptionPane.showMessageDialog(this, correct ? "Certo!" : "Errado.");

        gs.next();
        showQuestion();
        refreshScoreboard();
    }

    private void refreshScoreboard() {

        StringBuilder sb = new StringBuilder("Placar: ");
        for (Map.Entry<String, Integer> e : gs.getScoreboard().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("  ");
        }
        lblInfo.setText(sb.toString());
    }

    /* ===== Timer ===== */

    private void startTimer() {

        secondsLeft = QUESTION_TIME_SEC;
        lblTimer.setText("Tempo: " + secondsLeft + "s");

        swingTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            lblTimer.setText("Tempo: " + Math.max(0, secondsLeft) + "s");

            if (secondsLeft <= 0) {
                // tempo esgotado -> fecha a pergunta e avanca
                stopTimer();
                questionActive = false;
                listOptions.setEnabled(false);
                btnAnswer.setEnabled(false);
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "Tempo esgotado para esta pergunta.");
                gs.next();
                showQuestion();
                refreshScoreboard();
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

    private void endUI() {
        stopTimer();
        gameOver = true;
        questionActive = false;

        lblQuestion.setText("Fim do quiz!");
        optionsModel.setOptions(java.util.Collections.emptyList());
        listOptions.setEnabled(false);
        btnAnswer.setEnabled(false);
        lblTimer.setText("Tempo: --s");

        StringBuilder sb = new StringBuilder("Fim do quiz!\n\nPontuacoes finais:\n");
        for (var e : gs.getScoreboard().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);
    }
}
