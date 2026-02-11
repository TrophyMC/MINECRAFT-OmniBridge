package de.mecrytv.omniBridge.manager;

import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.MaintenanceModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class MaintenanceManager {

    private final long TTL_MS = 30_000;
    private final AtomicLong lastMaintenanceMode = new AtomicLong(0);
    private volatile boolean cachedMaintenanceMode = false;
    private final OmniBridge plugin;

    public MaintenanceManager(OmniBridge plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> isMaintenanceActive() {
        long now = System.currentTimeMillis();

        if (now - lastMaintenanceMode.get() < TTL_MS) {
            return CompletableFuture.completedFuture(cachedMaintenanceMode);
        }

        return DatabaseAPI.<MaintenanceModel>get("maintenance", "maintenance_mode").thenApply(maintenanceModel ->  {
            boolean active = (maintenanceModel != null && maintenanceModel.isMaintenanceMode());

            this.cachedMaintenanceMode = active;
            this.lastMaintenanceMode.set(System.currentTimeMillis());

            return active;
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return cachedMaintenanceMode;
        });
    }

    public void invalidateCache() {
        this.lastMaintenanceMode.set(0);
    }
}
