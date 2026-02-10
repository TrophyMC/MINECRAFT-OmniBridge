package de.mecrytv.omniBridge.models;

import com.google.gson.JsonObject;
import java.sql.Timestamp;
import de.mecrytv.databaseapi.model.ICacheModel;

public class WhitelistModel implements ICacheModel {

    private String playerUUID;
    private String playerName;
    private Timestamp whitelistAt;

    public WhitelistModel() {}

    public WhitelistModel(String playerUUID, String playerName, Timestamp whitelistAt) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.whitelistAt = whitelistAt;
    }

    @Override
    public String getIdentifier() {
        return playerUUID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("playerName", playerName);
        if (whitelistAt != null) {
            json.addProperty("whitelistAt", whitelistAt.getTime());
        } else {
            json.addProperty("whitelistAt", System.currentTimeMillis());
        }
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.playerUUID = json.get("playerUUID").getAsString();
        if (json.has("playerName")) {
            this.playerName = json.get("playerName").getAsString();
        }
        this.whitelistAt = new Timestamp(json.get("whitelistAt").getAsLong());
    }

    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    public Timestamp getWhitelistAt() {
        return whitelistAt;
    }
    public void setWhitelistAt(Timestamp whitelistAt) {
        this.whitelistAt = whitelistAt;
    }
}
