package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

class HttpArtifactFetcher implements ArtifactFetcher {

    private final HttpClient httpClient;

    HttpArtifactFetcher() {
        this(HttpClient.newHttpClient());
    }

    HttpArtifactFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void download(String url, Path destination) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();

        try {
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            HttpResponse<Path> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(destination)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Failed to download artifact from URL: " + url);
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to download artifact from URL: " + url, exception);
        }
    }
}
