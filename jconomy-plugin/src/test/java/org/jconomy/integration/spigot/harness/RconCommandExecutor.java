package org.jconomy.integration.spigot.harness;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

class RconCommandExecutor implements CommandExecutor {

    private static final int AUTH_TYPE = 3;
    private static final int EXECUTE_TYPE = 2;

    private final String host;
    private final int port;
    private final String password;
    private final Duration timeout;

    RconCommandExecutor(String host, int port, String password, Duration timeout) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.password = Objects.requireNonNull(password, "password");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    @Override
    public CommandResult execute(String command) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeout.toMillis());
            socket.setSoTimeout((int) timeout.toMillis());

            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

            writePacket(output, 1, AUTH_TYPE, password);
            Packet authResponse = readPacket(input);
            if (authResponse.requestId == -1) {
                return new CommandResult(false, "RCON authentication failed.");
            }

            writePacket(output, 2, EXECUTE_TYPE, command);
            Packet commandResponse = readPacket(input);

            return new CommandResult(true, commandResponse.payload);
        } catch (IOException exception) {
            return new CommandResult(false, exception.getMessage() == null ? "RCON command failed." : exception.getMessage());
        }
    }

    private void writePacket(DataOutputStream output, int requestId, int type, String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        int length = 4 + 4 + payloadBytes.length + 2;

        output.writeInt(Integer.reverseBytes(length));
        output.writeInt(Integer.reverseBytes(requestId));
        output.writeInt(Integer.reverseBytes(type));
        output.write(payloadBytes);
        output.writeByte(0);
        output.writeByte(0);
        output.flush();
    }

    private Packet readPacket(DataInputStream input) throws IOException {
        int length = Integer.reverseBytes(input.readInt());
        int requestId = Integer.reverseBytes(input.readInt());
        int type = Integer.reverseBytes(input.readInt());

        byte[] payload = new byte[Math.max(0, length - 10)];
        input.readFully(payload);
        input.readByte();
        input.readByte();

        return new Packet(requestId, type, new String(payload, StandardCharsets.UTF_8));
    }

    private static final class Packet {

        private final int requestId;
        private final int type;
        private final String payload;

        private Packet(int requestId, int type, String payload) {
            this.requestId = requestId;
            this.type = type;
            this.payload = payload;
        }
    }
}
