package de.mecrytv.omniBridge.events;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.AntiBotModel;
import de.mecrytv.omniBridge.models.WhitelistModel;
import de.mecrytv.omniBridge.utils.TranslationUtils;

import java.util.concurrent.CompletableFuture;

public class AntiBotListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();
        String playerUUID = player.getUniqueId().toString();
        var manager = OmniBridge.getInstance().getAntiBotManager();

        manager.checkRate();

        if (manager.isSentryModeActive()) {
            if (manager.isVerifiedInRam(ipAddress)) {
                continuation.resume();
                return;
            }

            DatabaseAPI.<AntiBotModel>get("antibot", ipAddress).thenCompose(botModel -> {
                if (botModel != null && botModel.getVerifiedAt() > 0) {
                    return CompletableFuture.completedFuture(true);
                }

                return DatabaseAPI.<WhitelistModel>get("whitelist", playerUUID)
                        .thenApply(wl -> wl != null);
            }).thenAccept(isAllowed -> {
                if (isAllowed) {
                    manager.addVerified(ipAddress);
                    continuation.resume();
                } else {
                    var msg = TranslationUtils.getComponentTranslation(player, "listeners.antibot");
                    event.setResult(LoginEvent.ComponentResult.denied(msg));
                    continuation.resume();
                }
            }).exceptionally(ex -> {
                continuation.resume();
                return null;
            });
            return;
        }

        continuation.resume();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        String ipAddress = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        var manager = OmniBridge.getInstance().getAntiBotManager();

        if (!manager.isVerifiedInRam(ipAddress)) {
            manager.addVerified(ipAddress);
            DatabaseAPI.set("antibot", new AntiBotModel(ipAddress, System.currentTimeMillis()));
        }
    }
}