package com.minetree.task.steps;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.schematic.FillSchematic;
import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public final class PlaceStep implements Step {

    private static final int DEFAULT_TIMEOUT_TICKS = 200;

    private final BlockPos supportPos;
    private final Item item;

    private IBaritone baritone;
    private Block targetBlock;
    private boolean started;
    private int ticks;

    public PlaceStep(BlockPos supportPos, Item item) {
        this.supportPos = supportPos;
        this.item = item;
    }

    @Override
    public StepResult tick() throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null) {
            throw new MineTreeException("World unloaded mid-task.");
        }

        if (!(item instanceof BlockItem blockItem)) {
            throw new MineTreeException(item.getTranslationKey() + " is not a block item.");
        }

        targetBlock = blockItem.getBlock();

        BlockPos placePos = supportPos.up();

        if (mc.world.getBlockState(placePos).isOf(targetBlock)) {
            cleanup();
            return StepResult.DONE;
        }

        if (!started) {
            baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

            FillSchematic schematic = new FillSchematic(1, 1, 1, targetBlock.getDefaultState());

            baritone.getBuilderProcess().build("PlaceStep", schematic, placePos);

            started = true;
        }

        if (mc.world.getBlockState(placePos).isOf(targetBlock)) {
            cleanup();
            return StepResult.DONE;
        }

        if (++ticks > DEFAULT_TIMEOUT_TICKS) {
            cleanup();
            throw new MineTreeException("Timed out placing " + item.getTranslationKey());
        }

        return StepResult.IN_PROGRESS;
    }

    private void cleanup() {
    }

    @Override
    public String describe() {
        return "PlaceStep(" + item.getTranslationKey() + " on " + supportPos.toShortString() + ")";
    }
}