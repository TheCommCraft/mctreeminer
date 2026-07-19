package com.minetree.task;

import com.minetree.MineTreeException;
import com.minetree.util.BaritoneUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class TaskRunner {

    private static MineTreeTask active;

    private TaskRunner() {
    }

    public static boolean isBusy() {
        return active != null;
    }

    public static void start(MineTreeTask task) {
        active = task;
        info("Starting job (" + task.totalSteps() + " steps)...");
    }

    public static void tick() {
        if (active == null) return;
        try {
            boolean finished = active.tick();
            if (finished) {
                info("Done.");
                active = null;
            }
        } catch (MineTreeException e) {
            error("Failed: " + e.getMessage());
            BaritoneUtil.cancelEverything();
            active = null;
        } catch (Exception e) {
            error("Unexpected error: " + e.getMessage());
            BaritoneUtil.cancelEverything();
            active = null;
        }
    }

    public static void cancel() {
        if (active != null) {
            info("Cancelled.");
            BaritoneUtil.cancelEverything();
            active = null;
        }
    }

    private static void info(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[MineTree] " + msg), false);
        }
    }

    private static void error(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[MineTree] " + msg).formatted(net.minecraft.util.Formatting.RED), false);
        }
    }
}
