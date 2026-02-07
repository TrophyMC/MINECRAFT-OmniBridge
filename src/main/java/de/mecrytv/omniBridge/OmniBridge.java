package de.mecrytv.omniBridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.databaseapi.utils.DatabaseConfig;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.omniBridge.manager.ConfigManager;
import de.mecrytv.omniBridge.utils.LogWithColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "omni-bridge",
        name = "Omni-Bridge",
        version = "1.0.0",
        description = "A Tool Plugin for Velocity Proxy's to connect with Omni-Core",
        authors = {"MecryTV"}
)
public class OmniBridge {

    private static OmniBridge instance;
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    private ConfigManager config;
    private ConfigManager modt;
    private DatabaseAPI databaseAPI;
    private LanguageAPI languageAPI;
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("omni:bridge");

    @Inject
    public OmniBridge(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        instance = this;
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.config = new ConfigManager(dataDirectory, "config.json");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        startLog();
        Path langDir = Path.of("/home/minecraft/languages/");

        this.modt = new ConfigManager(dataDirectory, "modt.json");
        this.languageAPI = new LanguageAPI(langDir);

        DatabaseConfig dbConfig = new DatabaseConfig(
                config.getString("mariadb.host"),
                config.getInt("mariadb.port"),
                config.getString("mariadb.database"),
                config.getString("mariadb.username"),
                config.getString("mariadb.password"),
                config.getString("redis.host"),
                config.getInt("redis.port"),
                config.getString("redis.password")
        );

        this.databaseAPI = new DatabaseAPI(dbConfig);
        server.getChannelRegistrar().register(IDENTIFIER);
    }

    private void startLog(){
        String[] omniBridgeLogo = {
                " ██████╗ ███╗   ███╗███╗   ██╗██╗██████╗ ██████╗ ██╗██████╗  ██████╗ ███████╗",
                "██╔═══██╗████╗ ████║████╗  ██║██║██╔══██╗██╔══██╗██║██╔══██╗██╔════╝ ██╔════╝",
                "██║   ██║██╔████╔██║██╔██╗ ██║██║██████╔╝██████╔╝██║██║  ██║██║  ███╗█████╗  ",
                "██║   ██║██║╚██╔╝██║██║╚██╗██║██║██╔══██╗██╔══██╗██║██║  ██║██║   ██║██╔══╝  ",
                "╚██████╔╝██║ ╚═╝ ██║██║ ╚████║██║██████╔╝██║  ██║██║██████╔╝╚██████╔╝███████╗",
                " ╚═════╝ ╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝  ╚═════╝ ╚══════╝",
                "                                                                             ",
                "                                 OmniBridge                                  ",
                "                            Running on Velocity                              "
        };
        for (String line: omniBridgeLogo) {
            logger.info(LogWithColor.color(line, LogWithColor.GREEN));
        }
        logger.info(LogWithColor.color("Developed by MecryTv", LogWithColor.GOLD));
        logger.info(LogWithColor.color("Plugin has been enabled!", LogWithColor.GREEN));
    }

    public static OmniBridge getInstance() {
        return instance;
    }
    public Logger getLogger() {
        return logger;
    }
    public ProxyServer getServer() {
        return server;
    }
    public Path getDataDirectory() {
        return dataDirectory;
    }
    public ConfigManager getConfig() {
        return config;
    }
    public ConfigManager getModt() {
        return modt;
    }
    public Component getPrefix() {
        if (!this.config.contains("prefix")) {
            return MiniMessage.miniMessage().deserialize("<dark_grey>[<gold>Omni<dark_grey>] ");
        }

        String prefixRaw = this.config.getString("prefix");

        if (prefixRaw == null || prefixRaw.isEmpty()) {
            return MiniMessage.miniMessage().deserialize("<dark_grey>[<gold>Omni<dark_grey>] ");
        }

        return MiniMessage.miniMessage().deserialize(prefixRaw);
    }
    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
    public LanguageAPI getLanguageAPI() {
        return languageAPI;
    }
}
