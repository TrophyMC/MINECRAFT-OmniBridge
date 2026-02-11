package de.mecrytv.omniBridge.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.MaintenanceModel;
import de.mecrytv.omniBridge.utils.GeneralUtils;
import de.mecrytv.omniBridge.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MaintenanceCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!GeneralUtils.isPlayer(source)) {
            TranslationUtils.sendTranslation(source, "commands.maintenance.only_players");
            return;
        }

        if (!source.hasPermission("omni.maintenance")) {
            TranslationUtils.sendTranslation(source, "commands.maintenance.no_permission");
            return;
        }

        if (args.length == 0) {
            sendUsage(source);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> handleOn(source);
            case "off" -> handleOff(source);
            default -> sendUsage(source);
        }
    }

    private void sendUsage(CommandSource source){
        List.of("header", "on", "off").forEach(key -> {
            TranslationUtils.sendTranslation(source, "commands.maintenance.usage." + key);
        });
    }
    private void handleOn(CommandSource source) {
        processMaintenanceAction(source, true);
    }
    private void handleOff(CommandSource source) {
        processMaintenanceAction(source, false);
    }

    private void processMaintenanceAction(CommandSource source, boolean targetState) {
        DatabaseAPI.<MaintenanceModel>get("maintenance", "maintenance_mode").thenAccept(result -> {
            boolean currentState = (result != null) && result.isMaintenanceMode();

            if (targetState == currentState) {
                String messageKey = targetState ? "already_active" : "already_inactive";
                TranslationUtils.sendTranslation(source, "commands.maintenance." + messageKey);
                return;
            }

            MaintenanceModel newModel = new MaintenanceModel(
                    targetState,
                    ((Player) source).getUsername(),
                    System.currentTimeMillis()
            );

            DatabaseAPI.set("maintenance", newModel);
            OmniBridge.getInstance().getMaintenanceManager().setCachedMaintenanceMode(targetState);

            String successKey = targetState ? "activated" : "deactivated";
            kickAllPlayers(targetState);
            TranslationUtils.sendTranslation(source, "commands.maintenance." + successKey);
        });
    }
    private void kickAllPlayers(boolean targetState) {
        if (!targetState) return;

        AtomicInteger counter = new AtomicInteger(5);
        var server = OmniBridge.getInstance().getServer();

        server.getScheduler().buildTask(OmniBridge.getInstance(), scheduledTask -> {
            int secondsLeft = counter.getAndDecrement();

            if (secondsLeft > 0) {
                for (Player player : server.getAllPlayers()) {
                    Component main = TranslationUtils.getComponentTranslation(player, "commands.maintenance.kick_title", "{seconds}", String.valueOf(secondsLeft));
                    Component sub = TranslationUtils.getComponentTranslation(player, "commands.maintenance.kick_subtitle");
                    player.showTitle(Title.title(main, sub));
                }
            } else {
                for (Player player : server.getAllPlayers()) {
                    if (!player.hasPermission("omni.maintenance.bypass")) {
                        player.disconnect(TranslationUtils.getComponentTranslation(player, "commands.maintenance.kick_message"));
                    }
                }
                scheduledTask.cancel();
            }
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }
}
