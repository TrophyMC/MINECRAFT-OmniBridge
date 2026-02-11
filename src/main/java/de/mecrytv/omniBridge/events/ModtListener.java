package de.mecrytv.omniBridge.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.mecrytv.omniBridge.OmniBridge;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;

public class ModtListener {

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        boolean isMaintenance = OmniBridge.getInstance().getMaintenanceManager().isMaintenanceActiveSync();

        ServerPing.Builder pingBuilder = event.getPing().asBuilder();
        MiniMessage miniMessage = MiniMessage.miniMessage();

        String mode = isMaintenance ? "maintenance" : "default";

        String topLine = OmniBridge.getInstance().getModt().getString(mode + ".top_line");
        String bottomLine = OmniBridge.getInstance().getModt().getString(mode + ".bottom_line");

        pingBuilder.description(miniMessage.deserialize(topLine + "\n" + bottomLine));

        if (isMaintenance) {
            pingBuilder.version(new ServerPing.Version(0, "§4Maintenance Mode"));

            pingBuilder.samplePlayers();

            pingBuilder.onlinePlayers(0);
            pingBuilder.maximumPlayers(0);
        }

        event.setPing(pingBuilder.build());
    }
}