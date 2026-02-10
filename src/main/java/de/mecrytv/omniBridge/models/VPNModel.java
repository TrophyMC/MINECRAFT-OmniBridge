package de.mecrytv.omniBridge.models;

import com.google.gson.JsonObject;
import de.mecrytv.databaseapi.model.ICacheModel;

public class VPNModel implements ICacheModel {

    private String ipAddress;
    private boolean isVPN;

    public VPNModel() {}

    public VPNModel(String ipAddress, boolean isVPN) {
        this.ipAddress = ipAddress;
        this.isVPN = isVPN;
    }

    @Override
    public String getIdentifier() {
        return ipAddress;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("ipAddress", ipAddress);
        json.addProperty("isVPN", isVPN);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.ipAddress = json.get("ipAddress").getAsString();
        this.isVPN = json.get("isVPN").getAsBoolean();
    }

    public boolean isVPN() {
        return isVPN;
    }
}
