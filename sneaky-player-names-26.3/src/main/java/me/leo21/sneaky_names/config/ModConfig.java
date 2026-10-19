package me.leo21.sneaky_names.config;

import me.leo21.sneaky_names.SneakyPlayerNames;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads config/sneaky_player_names.properties. The file format, keys and defaults are
 * identical to the 1.21 version, so existing config files keep working.
 */
public class ModConfig {
	private static final String FILE_NAME = "sneaky_player_names.properties";

	public boolean HIDE_PLAYER_NAMES = true;
	public int MAX_DISTANCE = 30;

	public ModConfig() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

		if (!Files.exists(file)) {
			SneakyPlayerNames.LOGGER.info("Config '{}' is missing, generating default one...", FILE_NAME);
			try {
				Files.createDirectories(file.getParent());
				Files.writeString(file, defaultConfig(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				SneakyPlayerNames.LOGGER.error("Config '{}' failed to generate!", FILE_NAME, e);
				return;
			}
		}

		Map<String, String> values = new HashMap<>();
		try {
			int lineNumber = 0;
			for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
				lineNumber++;
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] parts = line.split("[^\\S\\r\\n]*=[^\\S\\r\\n]*", 2);
				if (parts.length < 2) {
					SneakyPlayerNames.LOGGER.warn("Syntax error in config file on line {}!", lineNumber);
					continue;
				}
				values.put(parts[0], parts[1]);
			}
		} catch (IOException e) {
			SneakyPlayerNames.LOGGER.error("Config '{}' failed to load!", FILE_NAME, e);
			return;
		}

		String hide = values.get("hide_player_names");
		if (hide != null) HIDE_PLAYER_NAMES = hide.equalsIgnoreCase("true");

		String distance = values.get("max_distance");
		if (distance != null) {
			try {
				MAX_DISTANCE = Integer.parseInt(distance);
			} catch (NumberFormatException ignored) {
				// keep default
			}
		}
	}

	private String defaultConfig() {
		return """
				# Sneaky Player Names configuration file

				# Whether to hide out of sight players' names or not
				hide_player_names = %s

				# The furthest you can see a player name (in blocks).
				# A negative distance represents an infinite distance, and 0 will never show names.
				max_distance = %d

				""".formatted(HIDE_PLAYER_NAMES, MAX_DISTANCE);
	}
}
