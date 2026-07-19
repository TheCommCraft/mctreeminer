package com.minetree.command;

import com.minetree.MineTreeException;
import com.minetree.task.LargeTreeTask;
import com.minetree.task.MineTreeTask;
import com.minetree.task.SmallTreeTask;
import com.minetree.task.TaskRunner;
import com.minetree.tree.TreeDetector;
import com.minetree.tree.TreeInfo;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class MineTreeCommand {

    private MineTreeCommand() {
    }

    public static void register() {
        ClientCommandManager.getActiveDispatcher().register(literal("minetree").executes(ctx -> run(ctx.getSource(), null)).then(argument("x", IntegerArgumentType.integer()).then(argument("y", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> run(ctx.getSource(), new BlockPos(IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"))))))).then(literal("cancel").executes(ctx -> {
            TaskRunner.cancel();
            return 1;
        })));
    }

    private static int run(FabricClientCommandSource source, BlockPos explicitPos) {
        if (TaskRunner.isBusy()) {
            source.sendError(Text.literal("[MineTree] Already running a job - use /minetree cancel first."));
            return 0;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            source.sendError(Text.literal("[MineTree] No world loaded."));
            return 0;
        }

        BlockPos target = explicitPos;
        if (target == null) {
            target = raycastLog();
            if (target == null) {
                source.sendError(Text.literal("[MineTree] Look at a tree log, or supply /minetree <x> <y> <z>."));
                return 0;
            }
        }

        try {
            TreeInfo info = TreeDetector.detect(mc.world, target);
            MineTreeTask task = switch (info.type) {
                case SMALL -> SmallTreeTask.create(info);
                case LARGE -> LargeTreeTask.create(info);
            };
            TaskRunner.start(task);
            source.sendFeedback(Text.literal("[MineTree] Detected a " + info.type + " " + info.logBlock.getTranslationKey() + " tree. Starting."));
            return 1;
        } catch (MineTreeException e) {
            source.sendError(Text.literal("[MineTree] " + e.getMessage()).formatted(Formatting.RED));
            return 0;
        }
    }

    private static BlockPos raycastLog() {
        MinecraftClient mc = MinecraftClient.getInstance();
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        return null;
    }
}
