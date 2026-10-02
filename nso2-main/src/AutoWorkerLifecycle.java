/** Serial ownership across stop/start, even while an old route is unwinding. */
public final class AutoWorkerLifecycle {
    private volatile Thread owner;
    private final Object runLock = new Object();

    public synchronized Thread start(final Runnable task) {
        if (isActive()) return this.owner;
        Thread next = new Thread(new Runnable() {
            public void run() {
                Thread current = Thread.currentThread();
                try {
                    synchronized (runLock) {
                        if (isCurrent()) task.run();
                    }
                } finally {
                    synchronized (AutoWorkerLifecycle.this) {
                        if (owner == current) owner = null;
                    }
                }
            }
        });
        this.owner = next;
        next.start();
        return next;
    }

    public synchronized void stop() {
        Thread old = this.owner;
        this.owner = null;
        if (old != null) old.interrupt();
    }

    public boolean isActive() {
        Thread current = this.owner;
        return current != null && current.isAlive() && !current.isInterrupted();
    }

    public boolean isCurrent() {
        Thread current = Thread.currentThread();
        return this.owner == current && !current.isInterrupted();
    }
}
