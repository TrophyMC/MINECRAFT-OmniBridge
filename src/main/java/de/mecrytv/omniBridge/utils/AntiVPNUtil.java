package de.mecrytv.omniBridge.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.mecrytv.omniBridge.OmniBridge;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AntiVPNUtil {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<Boolean> isVPN(String ip){
        String apiKey = OmniBridge.getInstance().getConfig().getString("vpnAPIKey");

        if (apiKey == null || apiKey.isEmpty()) {
            OmniBridge.getInstance().getLogger().error("VPN API key is not set in the config. Please set 'vpnAPIKey' to use the VPN check feature.");
            return CompletableFuture.completedFuture(false);
        }

        String url = "https://proxycheck.io/v2/" + ip + "?key=" + apiKey + "&vpn=1";

        if (apiKey.isEmpty()) return CompletableFuture.completedFuture(false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
                   if (response.statusCode() == 200) {
                       JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                       if (json.has(ip)) {
                           JsonObject ipData = json.getAsJsonObject(ip);
                           return ipData.has("proxy") && ipData.get("proxy").getAsString().equalsIgnoreCase("yes");
                       }
                   }
                   return false;
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
        });
    }
}
