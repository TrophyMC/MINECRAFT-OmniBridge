package de.mecrytv.omniBridge.events;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.WhitelistModel;
import de.mecrytv.omniBridge.utils.TranslationUtils;
import net.kyori.adventure.text.Component;

public class ConnectionListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        OmniBridge.getInstance().getMaintenanceManager().isMaintenanceActive()
                .thenCompose(active -> {
                    return DatabaseAPI.<WhitelistModel>get("whitelist", uuid).thenAccept(whitelistEntry -> {
                        boolean isWhitelisted = (whitelistEntry != null);
                        boolean hasMaintBypass = player.hasPermission("omni.maintenance.bypass");

                        if (active) {
                            if (!hasMaintBypass && !isWhitelisted) {
                                Component msg = TranslationUtils.getComponentTranslation(player, "commands.maintenance.kick.message");
                                event.setResult(ResultedEvent.ComponentResult.denied(msg));
                            }
                        }
                    });
                })
                .whenComplete((res, ex) -> {
                    if (ex != null) ex.printStackTrace();
                    continuation.resume();
                });
    }
}
