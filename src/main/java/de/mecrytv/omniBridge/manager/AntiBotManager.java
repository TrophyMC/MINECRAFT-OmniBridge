package de.mecrytv.omniBridge.manager;

import de.mecrytv.databaseapi.DatabaseAPI;
import de.mecrytv.omniBridge.OmniBridge;
import de.mecrytv.omniBridge.models.AntiBotModel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AntiBotManager {

    private final OmniBridge plugin;

    private final int THRESHOLD = 5;
    private final long SENTRY_DURATION = 60_000;

    private final Set<String> verifiedIps = ConcurrentHashMap.newKeySet();

    private final AtomicInteger connectionsThisSecond = new AtomicInteger(0);
    private final AtomicLong lastSecond = new AtomicLong(System.currentTimeMillis() / 1000);
    private final AtomicLong sentryUntil = new AtomicLong(0);

    public AntiBotManager(OmniBridge plugin) {
        this.plugin = plugin;
    }

    public boolean isSentryModeActive() {
        return System.currentTimeMillis() < sentryUntil.get();
    }

    public boolean isVerifiedInRam(String ip) {
        return verifiedIps.contains(ip);
    }

    public void addVerified(String ip) {
        verifiedIps.add(ip);
    }

    public void checkRate() {
        long currentSecond = System.currentTimeMillis() / 1000;
        if (lastSecond.get() != currentSecond) {
            lastSecond.set(currentSecond);
            connectionsThisSecond.set(0);
        }

        if (connectionsThisSecond.incrementAndGet() > THRESHOLD) {
            sentryUntil.set(System.currentTimeMillis() + SENTRY_DURATION);
        }
    }
}