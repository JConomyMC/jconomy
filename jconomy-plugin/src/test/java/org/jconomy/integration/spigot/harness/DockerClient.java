package org.jconomy.integration.spigot.harness;

interface DockerClient {

    boolean imageExists(String imageName);

    void buildImage(String imageName);
}
