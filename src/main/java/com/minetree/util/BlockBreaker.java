package com.minetree.util;

import com.minetree.MineTreeException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class BlockBreaker {

    private final BlockPos pos;
    private final int timeoutTicks;
    private int ticksWorking = 0;

    public BlockBreaker(BlockPos pos, int timeoutTicks) {
        this.pos = pos;
        this.timeoutTicks = timeoutTicks;
    }

    public boolean tick() throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ClientPlayerInteractionManager im = mc.interactionManager;
        if (player == null || im == null || mc.world == null) {
            throw new MineTreeException("Player disconnected mid-task.");
        }

        if (mc.world.getBlockState(pos).isAir()) {
            im.cancelBlockBreaking();
            return true;
        }

        ticksWorking++;
        if (ticksWorking > timeoutTicks) {
            im.cancelBlockBreaking();
            throw new MineTreeException("Timed out breaking block at " + pos.toShortString() + " (unreachable or too hard?).");
        }

        faceBlock(player, pos);
        Direction face = Direction.UP;

        if (im.getCurrentGameMode() == GameMode.CREATIVE) {
            im.attackBlock(pos, face);
            return mc.world.getBlockState(pos).isAir();
        }

        if (ticksWorking <= 1) {
            im.attackBlock(pos, face);
        } else {
            im.updateBlockBreakingProgress(pos, face);
        }
        return mc.world.getBlockState(pos).isAir();
    }

    private static void faceBlock(ClientPlayerEntity player, BlockPos target) {
        Vec3d eye = player.getEyePos();
        Vec3d center = Vec3d.ofCenter(target);
        Vec3d diff = center.subtract(eye);
        double horizontalDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, horizontalDist));
        player.setYaw(yaw);
        player.setPitch(pitch);
    }
}
