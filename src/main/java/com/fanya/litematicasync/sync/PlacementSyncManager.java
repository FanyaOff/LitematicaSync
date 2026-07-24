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

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlacementSyncManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final PlacementSyncManager INSTANCE = new PlacementSyncManager();
    private static final int LOAD_DELAY_TICKS = 40;

    private final Path syncDirectory = FabricLoader.getInstance().getConfigDir().resolve("litematicasync").resolve("placements");

    // фикс фриза - создаем отдельный поток для записи/чтения, а не делаем все в основном потоке
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LitematicaSync-IO");
        t.setDaemon(true);
        return t;
    });

    private ServerGroup activeGroup;
    private int loadDelayTicks = -1;
    private int saveDelayTicks = -1;
    private boolean loadedGroupPlacements;
    private volatile Future<?> pendingSave;

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
        this.saveActiveGroupAsync();
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
            this.loadOrBootstrapActiveGroupAsync();
            this.loadDelayTicks = -1;
        }

        if (this.loadedGroupPlacements && LitematicaSyncConfig.get().periodicSave()) {
            if (this.saveDelayTicks < 0) {
                this.saveDelayTicks = LitematicaSyncConfig.get().periodicSaveTicks();
            }

            if (--this.saveDelayTicks <= 0) {
                this.saveActiveGroupAsync();
                this.saveDelayTicks = LitematicaSyncConfig.get().periodicSaveTicks();
            }
        }
    }

    private void loadOrBootstrapActiveGroupAsync() {
        if (this.activeGroup == null) {
            return;
        }

        ServerGroup group = this.activeGroup;
        Path file = this.fileForGroup(group);

        if (!Files.exists(file)) {
            this.saveActiveGroupAsync();
            this.loadedGroupPlacements = true;
            return;
        }

        Future<?> save = this.pendingSave;

        this.ioExecutor.execute(() -> {
            try {
                if (save != null) {
                    try { save.get(); } catch (Exception ignored) {}
                }

                JsonObject placements;

                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonElement element = JsonParser.parseReader(reader);

                    if (!element.isJsonObject()) {
                        return;
                    }

                    JsonObject root = element.getAsJsonObject();
                    placements = root.has("placements") && root.get("placements").isJsonObject()
                            ? root.getAsJsonObject("placements")
                            : root;
                }

                Minecraft.getInstance().execute(() -> {
                    if (this.activeGroup != group) {
                        return;
                    }

                    try {
                        DataManager.getSchematicPlacementManager().loadFromJson(placements);
                        this.loadedGroupPlacements = true;
                        LitematicaSync.LOGGER.info("Loaded synced Litematica placements for group '{}'", group.name());
                    } catch (Exception e) {
                        LitematicaSync.LOGGER.error("Failed to apply synced placements for group '{}'", group.name(), e);
                    }
                });
            } catch (Exception e) {
                LitematicaSync.LOGGER.error("Failed to load synced placements for group '{}'", group.name(), e);
            }
        });
    }

    private void saveActiveGroupAsync() {
        if (this.activeGroup == null) {
            return;
        }

        ServerGroup group = this.activeGroup;

        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("group", group.name());
        root.add("placements", DataManager.getSchematicPlacementManager().toJson());

        this.pendingSave = this.ioExecutor.submit(() -> {
            try {
                Files.createDirectories(this.syncDirectory);

                try (Writer writer = Files.newBufferedWriter(this.fileForGroup(group))) {
                    GSON.toJson(root, writer);
                }
            } catch (Exception e) {
                LitematicaSync.LOGGER.error("Failed to save synced placements for group '{}'", group.name(), e);
            }
        });
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
