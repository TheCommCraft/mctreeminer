package com.minetree.util;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.utils.IPlayerContext;
import net.minecraft.util.math.BlockPos;

public final class BaritoneUtil {

    private BaritoneUtil() {
    }

    private static ICustomGoalProcess process;

    public static IBaritone primary() {
        return BaritoneAPI.getProvider().getPrimaryBaritone();
    }

    public static IPlayerContext ctx() {
        return primary().getPlayerContext();
    }

    /**
     * Sends the player toward the given goal. Non-blocking; poll {@link #isIdle()}.
     */
    public static void pathTo(Goal goal) {
        process = primary().getCustomGoalProcess();
        process.setGoalAndPath(goal);
    }

    public static void pathToBlock(BlockPos pos) {
        pathTo(new GoalBlock(pos));
    }

    public static void pathNear(BlockPos pos, int range) {
        pathTo(new GoalNear(pos, range));
    }

    public static boolean isIdle() {
        return process == null || !process.isActive();
    }

    public static void cancelEverything() {
        primary().getPathingBehavior().cancelEverything();
        primary().getCustomGoalProcess().onLostControl();
    }

    public static double distanceTo(BlockPos pos) {
        return Math.sqrt(ctx().player().getBlockPos().getSquaredDistance(pos));
    }
}
