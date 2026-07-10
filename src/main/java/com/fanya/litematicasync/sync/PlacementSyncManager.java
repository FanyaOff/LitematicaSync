package com.fanya.litematicasync.sync;

import com.fanya.litematicasync.LitematicaSync;
import com.fanya.litematicasync.config.LitematicaSyncConfig;
import com.fanya.litematicasync.config.ServerGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.dy.masa.litematica.data.DataManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public class PlacementSyncManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final PlacementSyncManager INSTANCE = new PlacementSyncManager();
    private static final int LOAD_DELAY_TICKS = 40;

    private final Path syncDirectory = FabricLoader.getInstance().getConfigDir().resolve("litematicasync").resolve("placements");
    private ServerGroup activeGroup;
    private int loadDelayTicks = -1;
    private int saveDelayTicks = -1;
    private boolean loadedGroupPlacements;

    public static PlacementSyncManager getInstance() {
        return INSTANCE;
    }

    public void onJoin(Minecraft client) {
        ServerData serverData = client.getCurrentServer();

        if (serverData == null || serverData.ip == null || serverData.ip.isBlank()) {
            this.activeGroup = null;
            return;
        }

        Optional<ServerGroup> group = LitematicaSyncConfig.get().findGroup(serverData.ip);
        this.activeGroup = group.orElse(null);
        this.loadedGroupPlacements = false;

        if (this.activeGroup != null) {
            this.loadDelayTicks = LOAD_DELAY_TICKS;
            this.saveDelayTicks = LitematicaSyncConfig.get().periodicSaveTicks();
            LitematicaSync.LOGGER.info("Matched server '{}' to Litematica placement group '{}'", serverData.ip, this.activeGroup.name());
        }
    }

    public void onDisconnect() {
        this.saveActiveGroup();
        this.activeGroup = null;
        this.loadDelayTicks = -1;
        this.saveDelayTicks = -1;
        this.loadedGroupPlacements = false;
    }

    public void onClientTick(Minecraft client) {
        if (this.activeGroup == null || client.level == null) {
            return;
        }

        if (this.loadDelayTicks >= 0 && --this.loadDelayTicks <= 0) {
            this.loadOrBootstrapActiveGroup();
            this.loadDelayTicks = -1;
        }

        if (this.loadedGroupPlacements && LitematicaSyncConfig.get().periodicSave()) {
            if (this.saveDelayTicks < 0) {
                this.saveDelayTicks = LitematicaSyncConfig.get().periodicSaveTicks();
            }

            if (--this.saveDelayTicks <= 0) {
                this.saveActiveGroup();
                this.saveDelayTicks = LitematicaSyncConfig.get().periodicSaveTicks();
            }
        }
    }

    private void loadOrBootstrapActiveGroup() {
        if (this.activeGroup == null) {
            return;
        }

        Path file = this.fileForGroup(this.activeGroup);

        if (!Files.exists(file)) {
            this.saveActiveGroup();
            this.loadedGroupPlacements = true;
            return;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement element = JsonParser.parseReader(reader);

            if (!element.isJsonObject()) {
                return;
            }

            JsonObject root = element.getAsJsonObject();
            JsonObject placements = root.has("placements") && root.get("placements").isJsonObject()
                    ? root.getAsJsonObject("placements")
                    : root;

            DataManager.getSchematicPlacementManager().loadFromJson(placements);
            this.loadedGroupPlacements = true;
            LitematicaSync.LOGGER.info("Loaded synced Litematica placements for group '{}'", this.activeGroup.name());
        } catch (Exception e) {
            LitematicaSync.LOGGER.error("Failed to load synced placements for group '{}'", this.activeGroup.name(), e);
        }
    }

    private void saveActiveGroup() {
        if (this.activeGroup == null) {
            return;
        }

        try {
            Files.createDirectories(this.syncDirectory);

            JsonObject root = new JsonObject();
            root.addProperty("schema", 1);
            root.addProperty("group", this.activeGroup.name());
            root.add("placements", DataManager.getSchematicPlacementManager().toJson());

            try (Writer writer = Files.newBufferedWriter(this.fileForGroup(this.activeGroup))) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            LitematicaSync.LOGGER.error("Failed to save synced placements for group '{}'", this.activeGroup.name(), e);
        }
    }

    private Path fileForGroup(ServerGroup group) {
        return this.syncDirectory.resolve(group.name().replaceAll("[^a-zA-Z0-9._-]+", "_") + "-" + sha1(group.name()) + ".json");
    }

    private static String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
