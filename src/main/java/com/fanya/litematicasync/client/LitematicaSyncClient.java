package com.fanya.litematicasync.client;

import com.fanya.litematicasync.config.LitematicaSyncConfig;
import com.fanya.litematicasync.sync.PlacementSyncManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class LitematicaSyncClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LitematicaSyncConfig.load();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> PlacementSyncManager.getInstance().onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PlacementSyncManager.getInstance().onDisconnect());
        ClientTickEvents.END_CLIENT_TICK.register(client -> PlacementSyncManager.getInstance().onClientTick(client));
    }
}
