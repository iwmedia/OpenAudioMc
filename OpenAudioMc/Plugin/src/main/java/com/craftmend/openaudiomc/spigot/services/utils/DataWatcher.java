package com.craftmend.openaudiomc.spigot.services.utils;

import com.craftmend.openaudiomc.spigot.services.utils.interfaces.Feeder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DataWatcher<T> {

    private T value;
    private final @NotNull ScheduledTask task;
    private Feeder<T> dataFeeder;
    @Getter private Consumer<T> callback;
    private boolean isRunning;
    private boolean forced = false;

    public DataWatcher(JavaPlugin plugin, boolean sync, int delayTicks) {
        Runnable executor = () -> {
            if (this.dataFeeder == null) return;
            T newValue = dataFeeder.feed();
            if (forced || (this.value != null && !newValue.equals(this.value))) this.callback.accept(newValue);
            this.value = newValue;
            forced = false;
        };

        if (sync) {
            this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> executor.run(), delayTicks, delayTicks);
        } else {
            this.task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> executor.run(), delayTicks * 50L, delayTicks * 50L, TimeUnit.MILLISECONDS);
        }

        isRunning = true;
    }

    public DataWatcher<T> setFeeder(Feeder<T> feeder) {
        this.dataFeeder = feeder;
        return this;
    }

    public DataWatcher<T> setTask(Consumer<T> task) {
        this.callback = task;
        return this;
    }

    public void forceTicK() {
        this.forced = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() {
        this.task.cancel();
        this.isRunning = false;
    }

}
