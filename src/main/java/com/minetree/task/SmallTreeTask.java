package com.minetree.task;

import com.minetree.MineTreeException;
import com.minetree.task.steps.*;
import com.minetree.tree.TreeInfo;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class SmallTreeTask extends MineTreeTask {

    private static final int VERTICAL_REACH = 5;
    private static final int MOVE_TIMEOUT = 200;

    private SmallTreeTask(List<Step> steps) {
        super(steps);
    }

    public static SmallTreeTask create(TreeInfo info) throws MineTreeException {
        List<BlockPos> column = info.stemColumn;
        if (column.size() < 3) {
            throw new MineTreeException("This tree is too short for the small-tree technique (needs 3+ logs).");
        }

        BlockPos base = column.getFirst();
        List<Step> steps = new ArrayList<>();

        steps.add(MoveStep.near(base, 3, MOVE_TIMEOUT));

        steps.add(new BreakStep(column.get(1), null));
        steps.add(new BreakStep(column.get(2), null));

        steps.add(MoveStep.toExactCenter(base.up(), MOVE_TIMEOUT));

        int neededHeight = info.topY - base.getY() - 1;
        int climbCycles = Math.max(0, neededHeight - VERTICAL_REACH);
        for (int i = 0; i < climbCycles; i++) {
            steps.add(new BreakStep(column.get(i + 3), info.logBlock));
            steps.add(new ClimbOneStep());
        }

        for (int i = 1 + climbCycles; i < column.size(); i++) {
            steps.add(new BreakStep(column.get(i), null));
        }
        for (int i = climbCycles; i >= 0; i--) {
            steps.add(new BreakStep(column.get(i), null));
        }

        steps.add(MoveStep.near(base, 3, MOVE_TIMEOUT));
        steps.add(new PlaceStep(base.down(), info.saplingBlock.asItem()));

        steps.add(new WaitStep("wait"));

        return new SmallTreeTask(steps);
    }
}
