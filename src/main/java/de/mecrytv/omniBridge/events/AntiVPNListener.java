package de.mecrytv.omniBridge.events;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.models.VPNModel;
import de.mecrytv.omniBridge.utils.AntiVPNUtil;
import de.mecrytv.omniBridge.utils.TranslationUtils;
import net.kyori.adventure.text.Component;

public class AntiVPNListener {

    @Subscribe
    public void onLogin(LoginEvent event, Continuation continuation){
        Player player = event.getPlayer();

        if (player.hasPermission("omni.vpn.bypass")){
            continuation.resume();
            return;
        }

        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();
        Component kickMessage = TranslationUtils.getComponentTranslation(player, "listeners.antivpn.kick");

        DatabaseAPI.<VPNModel>get("vpn", ipAddress).thenAccept(result -> {
            if (result != null) {
                if (result.isVPN()) {
                    event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
                }
                continuation.resume();
            } else {
                AntiVPNUtil.isVPN(ipAddress).thenAccept(isVPN -> {
                    try {
                        VPNModel newModel = new VPNModel(ipAddress, isVPN);
                        DatabaseAPI.set("vpn", newModel);

                        if (isVPN) {
                            event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
                        }
                    } finally {
                        continuation.resume();
                    }
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    continuation.resume();
                    return null;
                });
            }
        });
    }
}
