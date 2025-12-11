package fr.esgi.service;

import java.util.concurrent.ScheduledFuture;

public interface SchedulerService {
    ScheduledFuture<?> schedule(Runnable task, long delayMs);
    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs);
    void shutdown();
}
