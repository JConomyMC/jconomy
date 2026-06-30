package org.jconomy.integration.spigot.harness;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpArtifactFetcherTests {

    @TempDir
    Path tempDir;

    @Test
    void downloadWritesResponseBodyToDestinationFile() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/artifact.jar", exchange -> {
            byte[] body = "artifact-content".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            HttpArtifactFetcher fetcher = new HttpArtifactFetcher();
            Path destination = tempDir.resolve("artifact.jar");

            fetcher.download("http://127.0.0.1:" + server.getAddress().getPort() + "/artifact.jar", destination);

            assertEquals("artifact-content", Files.readString(destination));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void downloadFailsForNonSuccessStatusCode() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/missing.jar", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();

        try {
            HttpArtifactFetcher fetcher = new HttpArtifactFetcher();
            Path destination = tempDir.resolve("artifact.jar");

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> fetcher.download("http://127.0.0.1:" + server.getAddress().getPort() + "/missing.jar", destination)
            );

            assertEquals("Failed to download artifact from URL: http://127.0.0.1:" + server.getAddress().getPort() + "/missing.jar", error.getMessage());
        } finally {
            server.stop(0);
        }
    }
}
