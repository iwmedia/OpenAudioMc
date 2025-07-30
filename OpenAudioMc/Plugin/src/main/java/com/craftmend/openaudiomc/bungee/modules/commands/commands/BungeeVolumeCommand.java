package com.craftmend.openaudiomc.bungee.modules.commands.commands;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.commands.helpers.CommandMiddewareExecutor;
import com.craftmend.openaudiomc.generic.commands.interfaces.CommandMiddleware;
import com.craftmend.openaudiomc.generic.commands.middleware.CatchCrashMiddleware;
import com.craftmend.openaudiomc.generic.commands.middleware.CatchLegalBindingMiddleware;
import com.craftmend.openaudiomc.generic.commands.middleware.CleanStateCheckMiddleware;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.user.adapters.BungeeUserAdapter;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.platform.Platform;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeVolumeCommand extends Command {

    /**
     * A set of middleware that to catch commands when the plugin isn't in a running state
     */
    private final CommandMiddleware[] commandMiddleware = new CommandMiddleware[]{
            new CatchLegalBindingMiddleware(),
            new CatchCrashMiddleware(),
            new CleanStateCheckMiddleware()
    };

    public BungeeVolumeCommand() {
        super("volume", "openaudiomc.commands.volume", "setvolume", "vol");
    }

    /**
     * Bungeecord specific implementation of the volume command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (CommandMiddewareExecutor.shouldBeCanceled(new BungeeUserAdapter(sender), null, commandMiddleware))
            return;

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("This command can only be used by players");
            return;
        }

        ClientConnection clientConnection = OpenAudioMc.getService(NetworkingService.class).getClient(((ProxiedPlayer) sender).getUniqueId());

        if (!clientConnection.isConnected()) {
            sender.sendMessage(Platform.translateColors(
                    StorageKey.MESSAGE_CLIENT_VOLUME_INVALID.getString())
            );
            return;
        }

        if (args.length == 0) {
            String message = Platform.translateColors(OpenAudioMc.getInstance().getConfiguration().getString(StorageKey.MESSAGE_CLIENT_VOLUME_INVALID));
            sender.sendMessage(message);
            return;
        }

        try {
            int volume = Integer.parseInt(args[0]);
            //check if in range
            if (volume < 0 || volume > 100) {
                String message = Platform.translateColors(OpenAudioMc.getInstance().getConfiguration().getString(StorageKey.MESSAGE_CLIENT_VOLUME_INVALID));
                sender.sendMessage(message);
                return;
            } else {
                clientConnection.setVolume(volume);
            }
        } catch (Exception e) {
            String message = Platform.translateColors(OpenAudioMc.getInstance().getConfiguration().getString(StorageKey.MESSAGE_CLIENT_VOLUME_INVALID));
            sender.sendMessage(message);
            return;
        }

    }
}
