package iskahoot.concurrent;

/**
 * CountDownLatch simples com bónus para as primeiras submissões e timeout.
 * Devolve fator de pontuação em countdown() e liberta await() por contador ou tempo.
 */
public class ModifiedCountdownLatch {
    private final int bonusFactor;
    private int bonusLeft;
    private final long waitPeriodMs;
    private int count;
    private boolean timedOut = false;

    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriodSeconds, int count) {
        this.bonusFactor = bonusFactor;
        this.bonusLeft = bonusCount;
        this.waitPeriodMs = waitPeriodSeconds * 1000L;
        this.count = count;
    }

    /**
     * Desce o contador. Devolve o fator a aplicar à pontuação desta submissão.
     */
    public synchronized int countdown() {
        if (count == 0) return 1; // já libertou
        int factor = (bonusLeft > 0) ? bonusFactor : 1;
        if (bonusLeft > 0) bonusLeft--;
        count = Math.max(0, count - 1);
        if (count == 0) notifyAll();
        return factor;
    }

    /**
     * Bloqueia até count chegar a 0 ou o tempo esgotar.
     */
    public synchronized void await() throws InterruptedException {
        long deadline = System.currentTimeMillis() + waitPeriodMs;
        while (count > 0 && !timedOut) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                timedOut = true;
                break;
            }
            wait(remaining);
        }
    }

    public synchronized boolean timedOut() {
        return timedOut;
    }

    public synchronized void expire() {
        timedOut = true;
        count = 0;
        notifyAll();
    }
}
