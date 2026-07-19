package com.minetree.task;

import baritone.api.BaritoneAPI;
import com.minetree.MineTreeException;
import com.minetree.task.steps.*;
import com.minetree.tree.TreeInfo;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class LargeTreeTask extends MineTreeTask {

    private static final int MOVE_TIMEOUT = 200;


    private LargeTreeTask(List<Step> steps) {
        super(steps);
    }

    public static LargeTreeTask create(TreeInfo info) throws MineTreeException {
        List<BlockPos> corners = info.stemBaseCorners;
        if (corners.size() != 4) {
            throw new MineTreeException("Internal error: large tree did not resolve to 4 trunk corners.");
        }

        BlockPos minCorner = corners.get(0);
        BlockPos maxCorner = corners.get(3);
        int minX = minCorner.getX();
        int minZ = minCorner.getZ();
        int maxX = maxCorner.getX();
        int maxZ = maxCorner.getZ();

        List<Step> steps = new ArrayList<>();

        BlockPos centerGround = new BlockPos((minX + maxX) / 2, info.baseY, (minZ + maxZ) / 2);
        steps.add(MoveStep.near(centerGround, 3, MOVE_TIMEOUT));

        List<BlockPos> treeSteps = new ArrayList<>();

        for (int y = info.baseY + 1; y <= info.topY; y++) {
            BlockPos cornerXZ = corners.get((y - info.baseY - 1) % 4);
            BlockPos stepPos = new BlockPos(cornerXZ.getX(), y, cornerXZ.getZ());
            steps.add(new BreakStep(stepPos, null));
            stepPos = stepPos.up();
            steps.add(new BreakStep(stepPos, null));
            stepPos = stepPos.up();
            steps.add(new BreakStep(stepPos, null));
            treeSteps.add(stepPos.down(2));
            if (y == info.topY) {
                treeSteps.add(stepPos.down(2));
                treeSteps.add(stepPos.down(2));
                treeSteps.add(stepPos.down(2));
            }
            steps.add(MoveStep.toExact(stepPos.down(2), MOVE_TIMEOUT));
        }

//        BlockPos topCenter = new BlockPos((minX + maxX) / 2, info.topY + 1, (minZ + maxZ) / 2);
//        steps.add(MoveStep.near(topCenter, 2, MOVE_TIMEOUT));

        int boxMinX = minX - 2, boxMaxX = maxX + 2;
        int boxMinZ = minZ - 2, boxMaxZ = maxZ + 2;
        for (int y = info.topY + 4; y >= info.baseY; y--) {
            for (int x = boxMinX; x <= boxMaxX; x++) {
                for (int z = boxMinZ; z <= boxMaxZ; z++) {
                    steps.add(new ConditionalBreakStep(new BlockPos(x, y, z), info.logBlock));
                }
            }
            if (y > info.baseY + 1) {
                steps.add(MoveStep.toExact(treeSteps.removeLast(), MOVE_TIMEOUT));
            }
        }

        steps.add(new WaitStep("wait"));

        steps.add(MoveStep.near(centerGround, 3, MOVE_TIMEOUT));


        for (BlockPos corner : corners) {
            steps.add(new PlaceStep(corner.down(), info.saplingBlock.asItem()));
        }

        steps.add(new WaitStep("wait"));

        return new LargeTreeTask(steps);
    }
}
