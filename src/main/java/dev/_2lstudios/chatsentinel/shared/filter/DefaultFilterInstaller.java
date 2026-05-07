package dev._2lstudios.chatsentinel.shared.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public final class DefaultFilterInstaller {
    public static final String CURRENT_VERSION = "2026-05-06-offense-v3";

    private static final List<String> MANAGED_RESOURCES = Arrays.asList(
            "blacklist/advertisement/domains.yml",
            "blacklist/client/hacked-clients.yml",
            "blacklist/offense/english.yml",
            "blacklist/offense/spanglish.yml",
            "blacklist/offense/spanish.yml",
            "blacklist/security/log4j.yml"
    );

    private DefaultFilterInstaller() {
    }

    public static void installIfNeeded(final File dataFolder, final ClassLoader classLoader, final Consumer<String> logger) {
        if (dataFolder == null || classLoader == null) {
            return;
        }
        final File marker = new File(dataFolder, ".default-filters-version");
        final String installed = readMarker(marker);
        if (CURRENT_VERSION.equals(installed)) {
            return;
        }

        final String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        for (final String resourcePath : MANAGED_RESOURCES) {
            installResource(dataFolder, classLoader, logger, resourcePath, stamp);
        }
        writeMarker(marker);
        log(logger, "Installed ChatSentinel managed default filters version " + CURRENT_VERSION + ". Existing managed files were backed up when overwritten.");
    }

    private static void installResource(final File dataFolder, final ClassLoader classLoader, final Consumer<String> logger,
            final String resourcePath, final String stamp) {
        final InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            log(logger, "Missing bundled filter resource: " + resourcePath);
            return;
        }
        try {
            final File target = new File(dataFolder, resourcePath.replace('/', File.separatorChar));
            final File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (target.exists()) {
                final File backup = new File(target.getParentFile(), target.getName() + ".bak." + stamp);
                Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            log(logger, "Unable to install managed filter resource " + resourcePath + ": " + exception.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (final IOException ignored) {
            }
        }
    }

    private static String readMarker(final File marker) {
        if (marker == null || !marker.exists()) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(marker.toPath()), StandardCharsets.UTF_8).trim();
        } catch (final IOException ignored) {
            return "";
        }
    }

    private static void writeMarker(final File marker) {
        try {
            final File parent = marker.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.write(marker.toPath(), CURRENT_VERSION.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException ignored) {
        }
    }

    private static void log(final Consumer<String> logger, final String message) {
        if (logger != null && message != null && !message.isEmpty()) {
            logger.accept(message);
        }
    }
}
