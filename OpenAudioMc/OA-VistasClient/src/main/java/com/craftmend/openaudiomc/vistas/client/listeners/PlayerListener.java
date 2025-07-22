package com.craftmend.openaudiomc.vistas.client.listeners;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.api.user.User;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.vistas.client.Vistas;
import com.craftmend.openaudiomc.vistas.client.client.VistasRedisClient;
import com.craftmend.openaudiomc.vistas.client.redis.packets.UserJoinPacket;
import com.craftmend.openaudiomc.vistas.client.redis.packets.UserLeavePacket;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private Map<UUID, ScheduledTask> joinCancels = new ConcurrentHashMap<>();
    private Vistas module;

    public PlayerListener(Vistas plugin) {
        this.module = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // delay one second to prevent fuckery lol
        @NotNull ScheduledTask task = Bukkit.getAsyncScheduler().runDelayed(OpenAudioMcSpigot.getInstance(), scheduledTask -> {
            // get user for this player
            User user = OpenAudioMc.resolveDependency(UserHooks.class).byUuid(event.getPlayer().getUniqueId());

            if (user == null) {
                OpenAudioLogger.warn("Vistas player join user is null");
                return;
            }

            OpenAudioMc.getService(VistasRedisClient.class).sendPacket(
                    new UserJoinPacket(
                            event.getPlayer().getName(),
                            event.getPlayer().getUniqueId(),
                            module.getServerId(),
                            user.getIpAddress()
                    )
            );
            joinCancels.remove(event.getPlayer().getUniqueId());
        }, 40 * 50, TimeUnit.MILLISECONDS); // 2 seconds
        joinCancels.put(event.getPlayer().getUniqueId(), task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (joinCancels.containsKey(event.getPlayer().getUniqueId())) {
            joinCancels.get(event.getPlayer().getUniqueId()).cancel();
            joinCancels.remove(event.getPlayer().getUniqueId());
        }

        OpenAudioMc.getService(VistasRedisClient.class).sendPacket(
                new UserLeavePacket(
                        event.getPlayer().getName(),
                        event.getPlayer().getUniqueId(),
                        module.getServerId()
                )
        );
    }

}
