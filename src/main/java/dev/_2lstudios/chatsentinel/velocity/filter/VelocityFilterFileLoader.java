package dev._2lstudios.chatsentinel.velocity.filter;

import dev._2lstudios.chatsentinel.shared.filter.FilterExpressionFile;
import dev._2lstudios.chatsentinel.shared.filter.FilterKind;
import dev._2lstudios.chatsentinel.shared.filter.FilterSource;
import dev._2lstudios.chatsentinel.velocity.utils.ConfigUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class VelocityFilterFileLoader {
    public List<FilterExpressionFile> load(FilterKind kind, Path dataDirectory, ConfigUtil configUtil) {
        String fileName = fileName(kind);
        String folderName = folderName(kind);
        Path folder = dataDirectory.resolve(folderName);
        List<FilterExpressionFile> files = new ArrayList<FilterExpressionFile>();

        configUtil.create(fileName);
        ensureDirectory(folder);
        copyDefaultFolderFiles(folderName, folder);

        files.add(loadFile(kind, dataDirectory.resolve(fileName), "default", fileName, displayName(kind, "default")));

        List<Path> folderFiles = listYamlFiles(folder);
        Collections.sort(folderFiles, new Comparator<Path>() {
            @Override
            public int compare(Path first, Path second) {
                return normalize(folder.relativize(first)).compareTo(normalize(folder.relativize(second)));
            }
        });

        for (Path path : folderFiles) {
            Path relative = folder.relativize(path);
            String moduleId = relative.getNameCount() == 0 ? "default" : relative.getName(0).toString();
            String relativePath = folderName + "/" + normalize(relative);
            files.add(loadFile(kind, path, moduleId, relativePath, moduleId));
        }

        return files;
    }

    private FilterExpressionFile loadFile(FilterKind kind, Path path, String moduleId, String relativePath, String displayName) {
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder().path(path).build();
            CommentedConfigurationNode root = loader.load();
            List<String> expressions = new ArrayList<String>();

            for (ConfigurationNode node : root.node("expressions").childrenList()) {
                String expression = node.getString();
                if (expression != null) {
                    expressions.add(expression);
                }
            }

            return new FilterExpressionFile(new FilterSource(kind, moduleId, relativePath, displayName), expressions);
        } catch (ConfigurateException e) {
            throw new IllegalStateException("Unable to load filter file " + path, e);
        }
    }

    private void ensureDirectory(Path folder) {
        try {
            if (Files.notExists(folder)) {
                Files.createDirectories(folder);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create filter folder " + folder, e);
        }
    }

    private void copyDefaultFolderFiles(String folderName, Path targetFolder) {
        ClassLoader classLoader = VelocityFilterFileLoader.class.getClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(folderName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    copyDefaultFolderFromPath(folderName, Paths.get(resource.toURI()), targetFolder);
                } else if ("jar".equals(resource.getProtocol())) {
                    copyDefaultFolderFromJar(folderName, resource, targetFolder);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy default filter folder " + folderName, e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to read default filter folder " + folderName, e);
        } catch (FileSystemNotFoundException e) {
            throw new IllegalStateException("Unable to access default filter folder " + folderName, e);
        }
    }

    private void copyDefaultFolderFromPath(String folderName, Path sourceFolder, Path targetFolder) throws IOException {
        if (!Files.isDirectory(sourceFolder)) {
            return;
        }

        List<Path> files = new ArrayList<Path>();
        collectYamlFiles(sourceFolder, files);
        for (Path source : files) {
            Path relative = sourceFolder.relativize(source);
            copyResourceIfMissing(folderName + "/" + normalize(relative), targetFolder.resolve(relative));
        }
    }

    private void copyDefaultFolderFromJar(String folderName, URL resource, Path targetFolder) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        JarFile jarFile = connection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();
        String prefix = folderName + "/";

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && name.startsWith(prefix) && name.endsWith(".yml")) {
                copyResourceIfMissing(name, targetFolder.resolve(name.substring(prefix.length())));
            }
        }
    }

    private void copyResourceIfMissing(String resourceName, Path target) throws IOException {
        if (Files.exists(target)) {
            return;
        }

        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        InputStream inputStream = VelocityFilterFileLoader.class.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream != null) {
            try {
                Files.copy(inputStream, target);
            } finally {
                inputStream.close();
            }
        }
    }

    private List<Path> listYamlFiles(Path folder) {
        List<Path> files = new ArrayList<Path>();
        try {
            collectYamlFiles(folder, files);
            return files;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to list filter folder " + folder, e);
        }
    }

    private void collectYamlFiles(Path folder, List<Path> files) throws IOException {
        if (!Files.isDirectory(folder)) {
            return;
        }

        java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(folder);
        try {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    collectYamlFiles(path, files);
                } else if (path.getFileName().toString().endsWith(".yml")) {
                    files.add(path);
                }
            }
        } finally {
            stream.close();
        }
    }

    private String fileName(FilterKind kind) {
        return folderName(kind) + ".yml";
    }

    private String folderName(FilterKind kind) {
        return kind == FilterKind.BLACKLIST ? "blacklist" : "whitelist";
    }

    private String displayName(FilterKind kind, String moduleId) {
        if (!"default".equals(moduleId)) {
            return moduleId;
        }
        return kind == FilterKind.BLACKLIST ? "Blacklist" : "Whitelist";
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }
}
