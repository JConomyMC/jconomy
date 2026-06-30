package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class Sha256Verifier {

    private Sha256Verifier() {
    }

    static void verify(Path file, String expectedSha256) {
        String actual = compute(file);
        if (!actual.equalsIgnoreCase(expectedSha256)) {
            throw new IllegalStateException("SHA-256 mismatch for " + file);
        }
    }

    static String compute(Path file) {
        MessageDigest digest = messageDigest();
        try (InputStream stream = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read file for SHA-256: " + file, exception);
        }

        byte[] hash = digest.digest();
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            hex.append(String.format("%02x", value));
        }

        return hex.toString();
    }

    private static MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable.", exception);
        }
    }
}
