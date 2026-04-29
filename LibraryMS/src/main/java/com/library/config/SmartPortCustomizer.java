package com.library.config;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SmartPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger(SmartPortCustomizer.class);
    private static final int DEFAULT_PORT = 8081;

    private final Environment environment;

    public SmartPortCustomizer(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        int requestedPort = environment.getProperty("server.port", Integer.class, DEFAULT_PORT);

        // If random port was requested explicitly, keep default Spring behavior.
        if (requestedPort == 0) {
            return;
        }

        if (isPortAvailable(requestedPort)) {
            factory.setPort(requestedPort);
            return;
        }

        int fallbackPort = findFreePort();
        factory.setPort(fallbackPort);
        log.warn(
                "Port {} is already in use. Automatically switching to free port {}.",
                requestedPort,
                fallbackPort);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not find a free local port for the web server.", ex);
        }
    }
}
