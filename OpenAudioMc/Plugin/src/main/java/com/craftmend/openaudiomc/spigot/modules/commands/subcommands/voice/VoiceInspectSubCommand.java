package com.craftmend.openaudiomc.spigot.modules.commands.subcommands.voice;

import com.craftmend.openaudiomc.generic.client.ClientDataService;
import com.craftmend.openaudiomc.generic.client.store.ClientDataStore;
import com.craftmend.openaudiomc.generic.commands.interfaces.SubCommand;
import com.craftmend.openaudiomc.generic.mojang.MojangLookupService;
import com.craftmend.openaudiomc.generic.mojang.store.MojangProfile;
import com.craftmend.openaudiomc.generic.rest.Task;
import com.craftmend.openaudiomc.generic.platform.OaColor;
import com.craftmend.openaudiomc.api.user.User;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VoiceInspectSubCommand extends SubCommand {

    public VoiceInspectSubCommand() {
        super("inspect");
        this.trimArguments = true;
    }

    @Override
    public void onExecute(User sender, String[] args) {
        if (!(sender.getOriginal() instanceof Player)) {
            message(sender, "Only players can open moderation menu's.");
            return;
        }

        if (args.length == 0) {
            message(sender, "Please specify the name of the player you want to inspect");
            return;
        }

        message(sender, "Fetching cached profile...");
        Task<MojangProfile> mojangFetch = getService(MojangLookupService.class).getByName(args[0]);

        mojangFetch.setWhenFailed((error) -> {
            message(sender, OaColor.RED + "There's no record of that player ever joining this server (" + error + ")");
        });

        mojangFetch.setWhenFinished(mojangProfile -> {
            message(sender, OaColor.GRAY + "Loading client data from " + mojangProfile.getUuid().toString() + "...");
            Task<ClientDataStore> clientDataRequest = getService(ClientDataService.class)
                    .getClientData(mojangProfile.getUuid(), true, false);

            clientDataRequest.setWhenFailed((error) -> {
                message(sender, OaColor.RED + "Failed to load profile data...");
            });

            clientDataRequest.setWhenFinished(clientDataStore -> {
                handleInspect(sender, args, clientDataStore, mojangProfile.getUuid(), mojangProfile.getName());
            });
        });
    }

    public void handleInspect(User sender, String[] args, ClientDataStore target, UUID targetId, String targetName) {
        message(sender, OaColor.GREEN + "Opening profile");
        Bukkit.getPlayer(sender.getUniqueId()).getScheduler().run(OpenAudioMcSpigot.getInstance(), task -> {
            new VoiceInspectGui((Player) sender.getOriginal(), target, targetId, targetName);
        }, null);
    }

}
