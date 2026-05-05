package dev._2lstudios.chatsentinel.bukkit.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.configuration.file.YamlConfiguration;

import dev._2lstudios.chatsentinel.bukkit.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.filter.FilterExpressionFile;
import dev._2lstudios.chatsentinel.shared.filter.FilterKind;
import dev._2lstudios.chatsentinel.shared.filter.FilterSource;

public final class BukkitFilterFileLoader {
	public List<FilterExpressionFile> load(FilterKind kind, File dataFolder, ConfigUtil configUtil) {
		if (kind == null || dataFolder == null || configUtil == null) {
			return Collections.emptyList();
		}

		String folderName = folderName(kind);
		String rootFileName = folderName + ".yml";
		configUtil.create("%datafolder%/" + rootFileName);

		File folder = new File(dataFolder, folderName);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		copyDefaultFolderFiles(folderName, folder);

		List<FilterExpressionFile> files = new ArrayList<FilterExpressionFile>();
		files.add(loadFile(kind, new File(dataFolder, rootFileName), "default", rootFileName));

		List<File> ymlFiles = new ArrayList<File>();
		collectYmlFiles(folder, ymlFiles);
		Collections.sort(ymlFiles, new Comparator<File>() {
			@Override
			public int compare(File first, File second) {
				return relativePath(folder, first).compareTo(relativePath(folder, second));
			}
		});

		for (File file : ymlFiles) {
			String relativeUnderFolder = relativePath(folder, file);
			String relativePath = folderName + "/" + relativeUnderFolder;
			String moduleId = moduleId(relativeUnderFolder);
			files.add(loadFile(kind, file, moduleId, relativePath));
		}

		return files;
	}

	private FilterExpressionFile loadFile(FilterKind kind, File file, String moduleId, String relativePath) {
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		FilterSource source = new FilterSource(kind, moduleId, relativePath, moduleId);
		return new FilterExpressionFile(source, configuration.getStringList("expressions"));
	}

	private void collectYmlFiles(File folder, List<File> files) {
		File[] children = folder.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				collectYmlFiles(child, files);
			} else if (child.isFile() && child.getName().endsWith(".yml")) {
				files.add(child);
			}
		}
	}

	private void copyDefaultFolderFiles(String folderName, File targetFolder) {
		try {
			Enumeration<URL> resources = BukkitFilterFileLoader.class.getClassLoader().getResources(folderName);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				if ("file".equals(url.getProtocol())) {
					copyDefaultFileResources(folderName, new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name())), targetFolder);
				} else if ("jar".equals(url.getProtocol())) {
					copyDefaultJarResources(folderName, url, targetFolder);
				}
			}
		} catch (IOException ignored) {
			// Missing bundled folders are optional
		}
	}

	private void copyDefaultFileResources(String folderName, File sourceFolder, File targetFolder) throws IOException {
		List<File> files = new ArrayList<File>();
		collectYmlFiles(sourceFolder, files);
		for (File file : files) {
			String relativePath = relativePath(sourceFolder, file);
			copyResource(folderName + "/" + relativePath, new File(targetFolder, relativePath));
		}
	}

	private void copyDefaultJarResources(String folderName, URL url, File targetFolder) throws IOException {
		JarURLConnection connection = (JarURLConnection) url.openConnection();
		JarFile jarFile = connection.getJarFile();
		Enumeration<JarEntry> entries = jarFile.entries();
		String prefix = folderName + "/";
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (!entry.isDirectory() && name.startsWith(prefix) && name.endsWith(".yml")) {
				copyResource(name, new File(targetFolder, name.substring(prefix.length())));
			}
		}
	}

	private void copyResource(String resourcePath, File targetFile) throws IOException {
		if (targetFile.exists()) {
			return;
		}
		File parent = targetFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		InputStream inputStream = BukkitFilterFileLoader.class.getClassLoader().getResourceAsStream(resourcePath);
		if (inputStream == null) {
			return;
		}
		try {
			Files.copy(inputStream, targetFile.toPath());
		} finally {
			inputStream.close();
		}
	}

	private static String folderName(FilterKind kind) {
		return kind == FilterKind.BLACKLIST ? "blacklist" : "whitelist";
	}

	private static String moduleId(String relativeUnderFolder) {
		int slashIndex = relativeUnderFolder.indexOf('/');
		String firstSegment = slashIndex < 0 ? relativeUnderFolder : relativeUnderFolder.substring(0, slashIndex);
		return firstSegment.endsWith(".yml") ? firstSegment.substring(0, firstSegment.length() - 4) : firstSegment;
	}

	private static String relativePath(File folder, File file) {
		return folder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
	}
}
