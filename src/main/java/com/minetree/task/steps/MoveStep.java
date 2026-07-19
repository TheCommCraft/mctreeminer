package com.minetree.task.steps;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import com.minetree.MineTreeException;
import com.minetree.task.Step;
import com.minetree.task.StepResult;
import com.minetree.util.BaritoneUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class MoveStep implements Step {

    private final Goal goal;
    private final int timeoutTicks;
    private boolean started = false;
    private boolean targetCenter;
    private int ticksElapsed = 0;

    public static MoveStep toExactCenter(BlockPos pos, int timeoutTicks) {
        return new MoveStep(new GoalBlock(pos), timeoutTicks, true);
    }

    public static MoveStep toExact(BlockPos pos, int timeoutTicks) {
        return new MoveStep(new GoalBlock(pos), timeoutTicks, false);
    }

    public static MoveStep near(BlockPos pos, int range, int timeoutTicks) {
        return new MoveStep(new GoalNear(pos, range), timeoutTicks, false);
    }

    private MoveStep(Goal goal, int timeoutTicks, boolean targetCenter) {
        this.goal = goal;
        this.timeoutTicks = timeoutTicks;
        this.targetCenter = targetCenter;
    }

    @Override
    public StepResult tick() throws MineTreeException {
        if (!started) {
            BaritoneUtil.pathTo(goal);
            started = true;
            return StepResult.IN_PROGRESS;
        }

        ticksElapsed++;
        if (ticksElapsed > timeoutTicks) {
            BaritoneUtil.cancelEverything();
            throw new MineTreeException("Timed out walking to position (path blocked or unreachable?).");
        }

        if (BaritoneUtil.isIdle()) {
            if (targetCenter) {
                GoalBlock goalBlock = (GoalBlock) goal;
                if (centerPlayer(MinecraftClient.getInstance().player, goalBlock.x, goalBlock.y, goalBlock.z)) {
                    return StepResult.DONE;
                }
                return StepResult.IN_PROGRESS;
            }
//            if (!goal.isInGoal(BaritoneUtil.ctx().playerFeet())) {
//                throw new MineTreeException("Could not path to the required position - no route found.");
//            }
            return StepResult.DONE;
        }
        return StepResult.IN_PROGRESS;
    }

    private boolean centerPlayer(ClientPlayerEntity player, int x, int y, int z) {
        double targetX = x + 0.5;
        double targetZ = z + 0.5;

        double dx = targetX - player.getX();
        double dz = targetZ - player.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 0.05) {
            MinecraftClient.getInstance().options.forwardKey.setPressed(false);
            return true;
        }

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        player.setYaw(yaw);

        MinecraftClient.getInstance().options.forwardKey.setPressed(true);
        return false;
    }

    @Override
    public String describe() {
        return "MoveStep(" + goal + ")";
    }
}
