package com.minetree;

import com.minetree.command.MineTreeCommand;
import com.minetree.task.TaskRunner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class MineTreeMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> MineTreeCommand.register());
        ClientTickEvents.END_CLIENT_TICK.register(client -> TaskRunner.tick());
    }
}
