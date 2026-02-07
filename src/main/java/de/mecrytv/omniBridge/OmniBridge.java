package de.mecrytv.omniBridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "omni-bridge",
        name = "Omni-Bridge",
        version = "1.0.0",
        description = "A Tool Plugin for Velocity Proxy's to connect with Omni-Core",
        authors = {"MecryTV"}
)
public class OmniBridge {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
