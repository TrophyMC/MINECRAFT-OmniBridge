package de.mecrytv.omniBridge.models;

import com.google.gson.JsonObject;
import de.mecrytv.databaseapi.model.ICacheModel;

public class MaintenanceModel implements ICacheModel {

    private boolean isMaintenanceMode;
    private String activatedBy;
    private long activatedAt;

    public MaintenanceModel() {}

    public MaintenanceModel(boolean isMaintenanceMode, String activatedBy, long activatedAt) {
        this.isMaintenanceMode = isMaintenanceMode;
        this.activatedBy = activatedBy;
        this.activatedAt = activatedAt;
    }

    @Override
    public String getIdentifier() {
        return "maintenance_mode";
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("isMaintenanceMode", isMaintenanceMode);
        json.addProperty("activatedBy", activatedBy);
        json.addProperty("activatedAt", activatedAt);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.isMaintenanceMode = json.get("isMaintenanceMode").getAsBoolean();
        this.activatedBy = json.get("activatedBy").getAsString();
        this.activatedAt = json.get("activatedAt").getAsLong();
    }

    public boolean isMaintenanceMode() {
        return isMaintenanceMode;
    }
    public void setMaintenanceMode(boolean maintenanceMode) {
        isMaintenanceMode = maintenanceMode;
    }
    public String getActivatedBy() {
        return activatedBy;
    }
    public void setActivatedBy(String activatedBy) {
        this.activatedBy = activatedBy;
    }
    public long getActivatedAt() {
        return activatedAt;
    }
    public void setActivatedAt(long activatedAt) {
        this.activatedAt = activatedAt;
    }
}
