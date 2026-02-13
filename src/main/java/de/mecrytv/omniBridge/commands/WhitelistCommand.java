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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class WhitelistCommand implements SimpleCommand {

    private WhitelistManager whitelistManager = OmniBridge.getInstance().getWhitelistManager();

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!GeneralUtils.isPlayer(source)) {
            TranslationUtils.sendTranslation(source, "commands.only_players");
            return;
        }

        if (!source.hasPermission("omni.whitelist")) {
            TranslationUtils.sendTranslation(source, "commands.no_permission");
            return;
        }

        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(source, args);
            case "remove" -> handleRemove(source, args);
            case "info" -> handleInfo(source);
            default -> sendUsage(source);
        }
    }

    private void sendUsage(CommandSource source){
        List.of("header", "add", "remove", "info").forEach(key -> {
            TranslationUtils.sendTranslation(source, "commands.whitelist.usage." + key);
        });
    }
    private void handleInfo(CommandSource source){
        whitelistManager.getWhitelistInfo(source);
    }
    private void handleAdd(CommandSource source, String[] args) {
        processPlayerAction(source, args, (uuid, name) ->
                whitelistManager.addToWhitelist(source, uuid, name));
    }
    private void handleRemove(CommandSource source, String[] args) {
        processPlayerAction(source, args, (uuid, name) ->
                whitelistManager.removeFromWhitelist(source, uuid, name));
    }

    private void processPlayerAction(CommandSource source, String[] args, BiConsumer<String, String> action){
        if (args.length < 2) {
            sendUsage(source);
            return;
        }

        String playerName = args[1];
        GeneralUtils.getOfflinePlayerID(playerName).thenAccept(playerUUID -> {
            if (playerUUID == null) {
                TranslationUtils.sendTranslation(source, "commands.player_not_found", "{player}", playerName);
                return;
            }

            action.accept(playerUUID, playerName);
        });
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
