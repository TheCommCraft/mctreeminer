package com.minetree.task.steps;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class BreakStep implements Step {

    private static final int DEFAULT_TIMEOUT_TICKS = 200;

    private final BlockPos pos;
    private final Block requiredBlock;

    private IBaritone baritone;
    private boolean started;
    private int ticks;

    public BreakStep(BlockPos pos) {
        this(pos, null);
    }

    public BreakStep(BlockPos pos, Block requiredBlock) {
        this.pos = pos;
        this.requiredBlock = requiredBlock;
    }

    @Override
    public StepResult tick() throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null) {
            throw new MineTreeException("World unloaded mid-task.");
        }

        if (mc.world.getBlockState(pos).isAir()) {
            cleanup();
            return StepResult.DONE;
        }

        if (!started && requiredBlock != null && mc.world.getBlockState(pos).getBlock() != requiredBlock) {
            cleanup();
            throw new MineTreeException("Expected " + requiredBlock + " at " + pos.toShortString() + " but found " + mc.world.getBlockState(pos).getBlock());
        }

        if (!started) {
            baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

            baritone.getBuilderProcess().clearArea(pos, pos);

            started = true;
        }

        ticks++;

        if ((!baritone.getBuilderProcess().isActive()) && mc.world.getBlockState(pos).isAir()) {
            cleanup();
            return StepResult.DONE;
        }

        if (ticks > DEFAULT_TIMEOUT_TICKS) {
            cleanup();
            throw new MineTreeException("Timed out breaking block at " + pos.toShortString());
        }

        return StepResult.IN_PROGRESS;
    }

    private void cleanup() {
    }

    @Override
    public String describe() {
        return "BreakStep(" + pos.toShortString() + ")";
    }
}