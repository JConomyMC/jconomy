package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

class SystemPortAllocator implements PortAllocator {

    @Override
    public int nextPort() {
        try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to allocate a free localhost port.", exception);
        }
    }
}
