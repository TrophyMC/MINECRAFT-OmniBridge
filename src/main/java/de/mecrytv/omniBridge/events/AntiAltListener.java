package de.mecrytv.omniBridge.events;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge; // WICHTIG: Import für die Instanz
import de.mecrytv.omniBridge.models.AltAccountModel;
import de.mecrytv.omniBridge.utils.TranslationUtils;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AntiAltListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();

        if (player.hasPermission("omni.antialt.bypass")) {
            continuation.resume();
            return;
        }

        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();
        String playerUUID = player.getUniqueId().toString();

        int maxAccounts = 2;

        DatabaseAPI.<AltAccountModel>get("altaccount", ipAddress)
                .thenCompose(model -> {
                    AltAccountModel currentModel = (model != null) ? model : new AltAccountModel(ipAddress, new ArrayList<>());

                    if (!currentModel.getKnownUuids().contains(playerUUID)) {
                        if (currentModel.getKnownUuids().size() >= maxAccounts) {
                            Component kickMsg = TranslationUtils.getComponentTranslation(player, "listeners.antialt");
                            event.setResult(LoginEvent.ComponentResult.denied(kickMsg));

                            return CompletableFuture.completedFuture(null);
                        }

                        currentModel.addUuid(playerUUID);
                        DatabaseAPI.set("altaccount", currentModel);
                    }

                    return CompletableFuture.completedFuture(null);
                })
                .whenComplete((res, ex) -> {
                    if (ex != null) ex.printStackTrace();
                    continuation.resume();
                });
    }
}