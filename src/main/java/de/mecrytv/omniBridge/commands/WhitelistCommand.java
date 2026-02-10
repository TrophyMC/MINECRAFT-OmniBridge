package de.mecrytv.omniBridge.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.manager.WhitelistManager;
import de.mecrytv.omniBridge.utils.GeneralUtils;
import de.mecrytv.omniBridge.utils.TranslationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WhitelistCommand implements SimpleCommand {

    private WhitelistManager whitelistManager = OmniBridge.getInstance().getWhitelistManager();

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!GeneralUtils.isPlayer(source)) {
            TranslationUtils.sendTranslation(source, "commands.whitelist.only_players");
            return;
        }

        Player player = (Player) source;

        if (!player.hasPermission("omni.whitelist")) {
            TranslationUtils.sendTranslation(player, "commands.whitelist.no_permission");
            return;
        }

        if (args.length == 0) {
            List.of("header", "add", "remove", "info").forEach(key -> {
                TranslationUtils.sendTranslation(source, "commands.whitelist.usage." + key);
            });
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 2) {
                    TranslationUtils.sendTranslation(source, "commands.whitelist.usage");
                    return;
                }

                String playerNameToAdd = args[1];
                GeneralUtils.getOfflinePlayerID(playerNameToAdd).thenAccept(playerUUID -> {
                    if (playerUUID == null) {
                        TranslationUtils.sendTranslation(source, "commands.whitelist.player_not_found", "{player}", playerNameToAdd);
                        return;
                    }

                    whitelistManager.addToWhitelist(source, playerUUID, playerNameToAdd);
                });
                break;
            case "remove":
                if (args.length < 2) {
                    TranslationUtils.sendTranslation(source, "commands.whitelist.usage");
                    return;
                }

                String playerNameToRemove = args[1];
                GeneralUtils.getOfflinePlayerID(playerNameToRemove).thenAccept(playerUUID -> {
                    if (playerUUID == null) {
                        TranslationUtils.sendTranslation(source, "commands.whitelist.player_not_found", "{player}", playerNameToRemove);
                        return;
                    }

                    whitelistManager.removeFromWhitelist(source, playerUUID, playerNameToRemove);
                });
                break;
            case "info":
                whitelistManager.getWhitelistInfo(source);
                break;
            default:
                List.of("header", "add", "remove", "info").forEach(key -> {
                    TranslationUtils.sendTranslation(source, "commands.whitelist.usage." + key);
                });
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String args[] = invocation.arguments();

        if (args.length <= 1) {
            String search = (args.length == 1) ? args[0].toLowerCase() : "";
            return List.of("add", "remove", "info").stream()
                    .filter(s -> s.startsWith(search)).collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            String search = args[1].toLowerCase();
            return OmniBridge.getInstance().getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(search)).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
