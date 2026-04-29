package com.library.config;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "app.browser.open-on-start", havingValue = "true")
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        var ctx = event.getApplicationContext();
        if (!(ctx instanceof WebServerApplicationContext webCtx)) {
            return;
        }
        int port = webCtx.getWebServer().getPort();
        if (port <= 0) {
            return;
        }
        String host = webCtx.getEnvironment().getProperty("app.browser.host", "localhost");
        String contextPath = webCtx.getEnvironment().getProperty("server.servlet.context-path", "");
        if (!StringUtils.hasText(contextPath)) {
            contextPath = "";
        } else if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        String url = "http://" + host + ":" + port + contextPath + "/";

        if (tryOpenWithDesktop(url)) {
            return;
        }
        if (tryOpenWithOsCommand(url)) {
            log.info("Opened default browser via OS command: {}", url);
            return;
        }
        log.warn("Could not open browser at {} (Desktop unavailable and OS command failed).", url);
    }

    private static boolean tryOpenWithDesktop(String url) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.debug("java.awt.Desktop browse not available; will try OS browser command.");
            return false;
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
            return true;
        } catch (Exception e) {
            log.debug("Desktop.browse failed ({}); will try OS browser command.", e.getMessage());
            return false;
        }
    }

    private static boolean tryOpenWithOsCommand(String url) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "start", "", url);
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", url);
        } else {
            pb = new ProcessBuilder("xdg-open", url);
        }
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        try {
            Process p = pb.start();
            p.getInputStream().close();
            return true;
        } catch (IOException e) {
            log.debug("OS browser command failed: {}", e.getMessage());
            return false;
        }
    }
}
