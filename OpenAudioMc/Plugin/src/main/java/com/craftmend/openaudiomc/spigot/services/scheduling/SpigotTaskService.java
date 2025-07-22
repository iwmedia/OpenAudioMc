package com.craftmend.openaudiomc.spigot.services.scheduling;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SpigotTaskService implements TaskService {

    private final ConcurrentMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

    @Override
    public int scheduleAsyncRepeatingTask(Runnable runnable, int delayUntilFirst, int tickInterval) {
        if (OpenAudioMc.getInstance().isDisabled()) {
            runnable.run();
            return -1;
        }

        return this.putTask(Bukkit.getAsyncScheduler().runAtFixedRate(OpenAudioMcSpigot.getInstance(), task -> {
            runnable.run();
        }, delayUntilFirst * 50L, tickInterval * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable runnable, int delayUntilFirst, int tickInterval) {
        if (OpenAudioMc.getInstance().isDisabled()) {
            runnable.run();
            return -1;
        }

        return this.putTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(OpenAudioMcSpigot.getInstance(), task -> {
            runnable.run();
        }, delayUntilFirst, tickInterval));
    }

    @Override
    public int schduleSyncDelayedTask(Runnable runnable, int delay) {
        if (OpenAudioMc.getInstance().isDisabled()) {
            runnable.run();
            return -1;
        }

        return this.putTask(Bukkit.getGlobalRegionScheduler().runDelayed(OpenAudioMcSpigot.getInstance(), task -> {
            runnable.run();
        }, delay));
    }

    @Override
    public void cancelRepeatingTask(int id) {
        this.getTask(id).ifPresent(ScheduledTask::cancel);
    }

    @Override
    public void runAsync(Runnable runnable) {
        if (OpenAudioMc.getInstance().isDisabled()) {
            this.notifyRunner();
            runnable.run();
            return;
        }

        Bukkit.getAsyncScheduler().runNow(OpenAudioMcSpigot.getInstance(), task -> runnable.run());
    }

    @Override
    public void runSync(Runnable runnable) {
        if (OpenAudioMc.getInstance().isDisabled()) {
            this.notifyRunner();
            runnable.run();
            return;
        }

        Bukkit.getGlobalRegionScheduler().run(OpenAudioMcSpigot.getInstance(), task -> runnable.run());
    }

    private int putTask(ScheduledTask task) {
        int randomId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        this.tasks.put(randomId, task);
        return randomId;
    }

    private Optional<ScheduledTask> getTask(int id) {
        if (!this.tasks.containsKey(id))
            return Optional.empty();
        return Optional.of(this.tasks.get(id));
    }
}
