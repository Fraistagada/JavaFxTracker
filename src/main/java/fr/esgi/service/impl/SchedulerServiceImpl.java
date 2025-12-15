package fr.esgi.service.impl;

import fr.esgi.service.SchedulerService;

import java.util.concurrent.*;

public class SchedulerServiceImpl implements SchedulerService {

    private volatile ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private synchronized void ensureRunning() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(2);
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delayMs) {
        ensureRunning();
        try {
            return scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            ensureRunning();
            return scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) {
        ensureRunning();
        try {
            return scheduler.scheduleAtFixedRate(task, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            ensureRunning();
            return scheduler.scheduleAtFixedRate(task, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
