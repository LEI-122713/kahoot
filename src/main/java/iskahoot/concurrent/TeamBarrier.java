package iskahoot.concurrent;

/**
 * Barreira simples com timeout (synchronized/wait/notifyAll).
 * Liberta quando todas as partes chegam ou o tempo expira, correndo a barrierAction.
 */
public class TeamBarrier {
    private final int parties;
    private final long waitPeriodMs;
    private final Runnable barrierAction;
    private int waiting = 0;
    private boolean released = false;

    public TeamBarrier(int parties, int waitPeriodSeconds, Runnable barrierAction) {
        this.parties = parties;
        this.waitPeriodMs = waitPeriodSeconds * 1000L;
        this.barrierAction = barrierAction;
    }

    public synchronized void await() throws InterruptedException {
        if (released) return;
        waiting++;
        if (waiting >= parties) {
            release();
            return;
        }

        long deadline = System.currentTimeMillis() + waitPeriodMs;
        while (!released) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                release();
                break;
            }
            wait(remaining);
        }
    }

    public synchronized void release() {
        if (released) return;
        released = true;
        if (barrierAction != null) barrierAction.run();
        notifyAll();
    }
}
