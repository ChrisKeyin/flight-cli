package ca.keyin.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FlightApiClient {
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public FlightApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.http = HttpClient.newHttpClient();
    }

    public String get(String path) throws Exception {
        String url = baseUrl + path;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 200 && res.statusCode() < 300) {
            return res.body();
        }
        throw new RuntimeException("GET " + url + " failed: " + res.statusCode() + " " + res.body());
    }

    public <T> T getAs(String path, Class<T> type) throws Exception {
        return mapper.readValue(get(path), type);
    }

    public <T> T getAs(String path, TypeReference<T> typeRef) throws Exception {
        return mapper.readValue(get(path), typeRef);
    }
}
