package com.fanya.litematicasync.config;

import com.fanya.litematicasync.LitematicaSync;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LitematicaSyncConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("litematicasync.json");
    private static LitematicaSyncConfig instance = createDefault();

    private List<ServerGroup> groups = new ArrayList<>();
    private boolean periodicSave = true;
    private int periodicSaveTicks = 200;

    public static LitematicaSyncConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            instance = createDefault();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            LitematicaSyncConfig loaded = GSON.fromJson(reader, LitematicaSyncConfig.class);
            instance = loaded == null ? createDefault() : loaded;
            instance.sanitize();
        } catch (IOException | JsonSyntaxException e) {
            LitematicaSync.LOGGER.error("Failed to load config '{}'", CONFIG_FILE.toAbsolutePath(), e);
            instance = createDefault();
        }
    }

    public static void save() {
        try {
            Path parent = CONFIG_FILE.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            LitematicaSync.LOGGER.error("Failed to save config '{}'", CONFIG_FILE.toAbsolutePath(), e);
        }
    }

    public Optional<ServerGroup> findGroup(String address) {
        return this.groups().stream()
                .filter(group -> group.containsAddress(address))
                .findFirst();
    }

    public List<ServerGroup> groups() {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }

        return this.groups;
    }

    public boolean periodicSave() {
        return this.periodicSave;
    }

    public int periodicSaveTicks() {
        return Math.max(20, this.periodicSaveTicks);
    }

    public List<String> toEditableLines() {
        List<String> lines = new ArrayList<>();

        for (ServerGroup group : this.groups()) {
            lines.add(group.toEditableLine());
        }

        return lines;
    }

    public void replaceGroupsFromEditableLines(List<String> lines) {
        List<ServerGroup> parsedGroups = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            ServerGroup group = ServerGroup.fromEditableLine(line);

            if (!group.name().isEmpty() && !group.addresses().isEmpty()) {
                parsedGroups.add(group);
            }
        }

        this.groups = parsedGroups;
        save();
    }

    public void replaceGroups(List<ServerGroup> groups) {
        this.groups = new ArrayList<>(groups);
        this.sanitize();
        save();
    }

    private void sanitize() {
        this.groups().removeIf(group -> group.name().isEmpty() || group.addresses().isEmpty());
        this.periodicSaveTicks = Math.max(20, this.periodicSaveTicks);
    }

    private static LitematicaSyncConfig createDefault() {
        LitematicaSyncConfig config = new LitematicaSyncConfig();
        config.groups.add(new ServerGroup("Pepeland", List.of(
            "play.pepeland.net",
            "issues.pepeland.net",
            "issues2.pepeland.net",
            "neo.play.pepeland.net"
        )));
        return config;
    }
}
