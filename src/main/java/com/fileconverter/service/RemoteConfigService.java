package com.fileconverter.service;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RemoteConfigService {

    private static final String CONFIG_URL =
            "https://gist.githubusercontent.com/Kausarroad2succes/6180137b53f4b836a7ba8d96dc8e5372/raw/03ecb3730692181894d471184f2e4717e0f5d964/premium-config.json";

    public static boolean verifyPremiumCode(String enteredCode) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CONFIG_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch config: HTTP " + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());
        String validCode = json.getString("premiumCode");

        return validCode.equals(enteredCode);
    }
}