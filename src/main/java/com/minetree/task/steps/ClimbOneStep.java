package com.minetree.task.steps;

import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import com.minetree.util.BlockPlacer;
import com.minetree.util.ScaffoldBlocks;

public final class ClimbOneStep implements Step {

    private enum Phase {PICK_ITEM, JUMP, RISING, PLACE, LANDING}

    private static final int RISE_TICKS = 3;
    private static final int LAND_TIMEOUT_TICKS = 40;

    private Phase phase = Phase.PICK_ITEM;
    private BlockPos originFeet;
    private Item item;
    private int ticksInPhase = 0;

    @Override
    public StepResult tick() throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) throw new MineTreeException("Player disconnected mid-task.");

        switch (phase) {
            case PICK_ITEM -> {
                item = ScaffoldBlocks.findOrGrant();
                originFeet = player.getBlockPos();
                phase = Phase.JUMP;
                return StepResult.IN_PROGRESS;
            }
            case JUMP -> {
                player.jump();
                phase = Phase.RISING;
                MinecraftClient.getInstance().player.setPitch(90.0F);
                ticksInPhase = 0;
                return StepResult.IN_PROGRESS;
            }
            case RISING -> {
                ticksInPhase++;
                if (ticksInPhase >= RISE_TICKS) {
                    phase = Phase.PLACE;
                }
                return StepResult.IN_PROGRESS;
            }
            case PLACE -> {
                BlockPlacer.placeOn(originFeet.down(), item);
                phase = Phase.LANDING;
                ticksInPhase = 0;
                return StepResult.IN_PROGRESS;
            }
            case LANDING -> {
                ticksInPhase++;
                if (player.isOnGround() && player.getBlockPos().getY() > originFeet.getY()) {
                    return StepResult.DONE;
                }
                if (ticksInPhase > LAND_TIMEOUT_TICKS) {
                    throw new MineTreeException("Failed to gain height while scaffolding up the trunk.");
                }
                return StepResult.IN_PROGRESS;
            }
        }
        throw new MineTreeException("Unreachable climb state.");
    }

    @Override
    public String describe() {
        return "ClimbOneStep(" + phase + ")";
    }
}
