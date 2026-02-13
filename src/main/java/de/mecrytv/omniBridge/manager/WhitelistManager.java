package de.mecrytv.omniBridge.manager;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.WhitelistModel;
import de.mecrytv.omniBridge.utils.TranslationUtils;
import net.kyori.adventure.text.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WhitelistManager {

    private final OmniBridge plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");

    public WhitelistManager(OmniBridge plugin) {
        this.plugin = plugin;
    }

    public void addToWhitelist(CommandSource source, String playerUUID, String playerName) {
        DatabaseAPI.<WhitelistModel>get("whitelist", playerUUID).thenAccept(isWhitelisted -> {
            if (isWhitelisted != null) {
                TranslationUtils.sendTranslation(source, "commands.whitelist.already_whitelisted", "{playerName}", playerName);
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            WhitelistModel whitelistModel = new WhitelistModel(
                    playerUUID,
                    playerName,
                    now
            );

            DatabaseAPI.set("whitelist", whitelistModel);
            TranslationUtils.sendTranslation(source, "commands.whitelist.added", "{player}", playerName);
        });
    }

    public void removeFromWhitelist(CommandSource source, String playerUUID, String playerName) {
        DatabaseAPI.<WhitelistModel>get("whitelist", playerUUID).thenAccept(isWhitelisted -> {
            if (isWhitelisted == null) {
                TranslationUtils.sendTranslation(source, "commands.whitelist.not_whitelisted", "{playerName}", playerName);
                return;
            }

            DatabaseAPI.delete("whitelist", playerUUID);
            TranslationUtils.sendTranslation(source, "commands.whitelist.removed", "{playerName}", playerName);
        });
    }

    public void getWhitelistInfo(CommandSource source) {
        DatabaseAPI.<WhitelistModel>getAll("whitelist").thenAccept(allWhitelisted -> {
            if (allWhitelisted.isEmpty()) {
                TranslationUtils.sendTranslation(source, "commands.whitelist.info.empty_whitelist");
                return;
            }

            List<Component> messageBlock = new ArrayList<>();
            messageBlock.add(TranslationUtils.getComponentTranslation((Player) (source instanceof Player ? source : null), "commands.whitelist.info.header"));

            for (WhitelistModel whitelistModel : allWhitelisted) {
                String displayName = whitelistModel.getPlayerName() != null ? whitelistModel.getPlayerName() : whitelistModel.getIdentifier();
                String whitelistAt = dateFormat.format(whitelistModel.getWhitelistAt());

                Component entry = TranslationUtils.getComponentTranslation(
                        (Player) (source instanceof Player ? source : null),
                        "commands.whitelist.info.entry",
                        "{playerName}", displayName,
                        "{timestamp}", whitelistAt
                );
                messageBlock.add(entry);
            }

            messageBlock.forEach(source::sendMessage);
        });
    }
}
