package iskahoot.client;

import iskahoot.model.Question;
import iskahoot.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Janela principal do jogo IsKahoot (cliente local, entrega intermédia).
 * Mostra perguntas, opções e placar, e permite responder.
 * Interage com o GameState para gerir o progresso e as pontuações.
 */

public class KahootClientWindow extends JFrame {

    // tempo máximo (segundos) por pergunta
    private static final int QUESTION_TIME_SEC = 30;

    // Componentes visuais da janela
    private final JLabel lblTitle = new JLabel("IsKahoot — Cliente (Entrega Intermédia)");
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

    /** Construtor principal */

    public KahootClientWindow(GameState gs, String myTeam) {
        super("IsKahoot — Cliente");
        this.gs = gs;
        this.myTeam = myTeam;
        initUI();              // monta a interface
        showQuestion();        // mostra a primeira pergunta
        refreshScoreboard();   // atualiza o placar
    }

    /** Configura todos os elementos visuais da janela */

    private void initUI() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // cabeçalho (título + timer)
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        lblTimer.setFont(lblTitle.getFont().deriveFont(Font.PLAIN, 14f));
        JPanel header = new JPanel(new BorderLayout());
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblTimer, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // zona central (pergunta + lista de opções)
        lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(lblQuestion, BorderLayout.NORTH);

        listOptions.setModel(optionsModel); // usa o modelo que já criaste
        listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(new JScrollPane(listOptions), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // zona inferior (placar + botão responder)
        JPanel south = new JPanel(new BorderLayout(0, 4));
        lblTeam.setText("A jogar como: " + myTeam);
        south.add(lblTeam, BorderLayout.NORTH);
        south.add(lblInfo, BorderLayout.CENTER);
        south.add(btnAnswer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // evento do botão
        btnAnswer.addActionListener(e -> onAnswer());

        setSize(680, 460);
        setLocationRelativeTo(null); // centra a janela no ecrã
    }

    /** Mostra a pergunta atual na interface */

    private void showQuestion() {

        stopTimer(); // parar qualquer timer anterior

        // se o jogo acabou ou já não há perguntas, termina
        if (gameOver || !gs.hasNext()) {
            endUI();
            return;
        }

        // obter pergunta atual
        Question q = gs.current();
        lblQuestion.setText("<html>" + q.question + " (" + q.points + " pts)</html>");
        optionsModel.setOptions(q.options);   // atualiza lista de opções
        listOptions.clearSelection();
        listOptions.setEnabled(true);
        btnAnswer.setEnabled(true);

        startTimer(); // iniciar contagem decrescente
    }

    /** Trata o clique no botão "Responder" */

    private void onAnswer() {

        if (gameOver || !gs.hasNext()) return;

        // se o tempo esgotou, não permite responder
        if (secondsLeft <= 0) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // verifica se alguma opção foi selecionada
        int sel = listOptions.getSelectedIndex();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Escolhe uma opção primeiro.");
            return;
        }

        // submete resposta ao GameState
        boolean correct = gs.submitAnswer(myTeam, sel);
        JOptionPane.showMessageDialog(this, correct ? "Certo!" : "Errado.");

        // avança para a próxima pergunta
        gs.next();
        showQuestion();
        refreshScoreboard();
    }

    /** Atualiza o texto do placar com as pontuações atuais */

    private void refreshScoreboard() {

        StringBuilder sb = new StringBuilder("Placar: ");
        for (Map.Entry<String, Integer> e : gs.getScoreboard().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("  ");
        }
        lblInfo.setText(sb.toString());
    }

    /* ===== Timer ===== */

    /** Inicia o temporizador da pergunta */

    private void startTimer() {

        secondsLeft = QUESTION_TIME_SEC;
        lblTimer.setText("Tempo: " + secondsLeft + "s");

        // timer Swing que executa de 1 em 1 segundo
        swingTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            lblTimer.setText("Tempo: " + Math.max(0, secondsLeft) + "s");

            if (secondsLeft <= 0) {
                // tempo esgotado -> termina o jogo
                stopTimer();
                gameOver = true;
                endUI();
                refreshScoreboard();
            }
        });
        swingTimer.setInitialDelay(1000);
        swingTimer.start();
    }

    /** Para o temporizador */

    private void stopTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    /** Mostra o ecrã de fim do jogo + popup com resultados */

    private void endUI() {
        stopTimer();
        gameOver = true;

        // muda o aspeto da interface
        lblQuestion.setText("Fim do quiz!");
        optionsModel.setOptions(java.util.Collections.emptyList());
        listOptions.setEnabled(false);
        btnAnswer.setEnabled(false);
        lblTimer.setText("Tempo: --s");

        // popup com o resultado final
        StringBuilder sb = new StringBuilder("Fim do quiz!\n\nPontuações finais:\n");
        for (var e : gs.getScoreboard().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);

        // opcional: fecha automaticamente
        // dispose(); // ou System.exit(0);
    }
}
