package com.minetree.tree;

import com.minetree.MineTreeException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TreeDetector {

    private TreeDetector() {
    }

    private static final Map<Block, Block> SAPLING_OF = Map.of(Blocks.OAK_LOG, Blocks.OAK_SAPLING, Blocks.BIRCH_LOG, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_LOG, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_LOG, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_LOG, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_LOG, Blocks.CHERRY_SAPLING);

    private static final List<Block> LARGE_TREE_LOGS = List.of(Blocks.SPRUCE_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG);

    private static final int MAX_TRUNK_SCAN = 48;

    public static TreeInfo detect(World world, BlockPos targeted) throws MineTreeException {
        Block targetBlock = world.getBlockState(targeted).getBlock();
        if (!isLog(targetBlock)) {
            throw new MineTreeException("Targeted block is not a tree log.");
        }
        if (!SAPLING_OF.containsKey(targetBlock)) {
            throw new MineTreeException("Unsupported log type: " + targetBlock.getTranslationKey());
        }

        BlockPos base = findColumnBase(world, targeted, targetBlock);

        List<BlockPos> largeCorners = tryFindLargeTrunkCorners(world, base, targetBlock);
        if (largeCorners != null) {
            if (!LARGE_TREE_LOGS.contains(targetBlock)) {
                throw new MineTreeException("2x2 trunk detected but " + targetBlock.getTranslationKey() + " large trees are not supported (only spruce/dark oak).");
            }
            int topY = findLargeTrunkTop(world, largeCorners, targetBlock, base.getY());
            return TreeInfo.large(targetBlock, SAPLING_OF.get(targetBlock), largeCorners, base.getY(), topY);
        }

        List<BlockPos> column = buildSmallColumnRejectingBranches(world, base, targetBlock);
        return TreeInfo.small(targetBlock, SAPLING_OF.get(targetBlock), column);
    }

    private static boolean isLog(Block block) {
        String path = net.minecraft.registry.Registries.BLOCK.getId(block).getPath();
        return path.endsWith("_log") && !path.startsWith("stripped_");
    }

    private static BlockPos findColumnBase(World world, BlockPos start, Block logBlock) {
        BlockPos pos = start;
        for (int i = 0; i < MAX_TRUNK_SCAN; i++) {
            BlockPos below = pos.down();
            if (world.getBlockState(below).getBlock() == logBlock) {
                pos = below;
            } else {
                break;
            }
        }
        return pos;
    }

    private static List<BlockPos> tryFindLargeTrunkCorners(World world, BlockPos base, Block logBlock) {
        int[][] offsets = new int[][]{{0, 0}, {1, 0}, {1, 1}, {0, 1}, {0, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, 0}, {0, -1}, {1, -1}, {1, 0}, {0, 0}, {-1, 0}, {-1, -1}, {0, -1}};
        for (int square = 0; square < 4; square++) {
            List<BlockPos> corners = new ArrayList<>(4);
            boolean ok = true;
            for (int i = 0; i < 4; i++) {
                int[] off = offsets[square * 4 + i];
                BlockPos p = base.add(off[0], 0, off[1]);
                corners.add(p);
                if (world.getBlockState(p).getBlock() != logBlock || world.getBlockState(p.up()).getBlock() != logBlock) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return corners;
            }
        }
        return null;
    }

    private static int findLargeTrunkTop(World world, List<BlockPos> corners, Block logBlock, int baseY) {
        int y = baseY;
        for (int i = 0; i < MAX_TRUNK_SCAN; i++) {
            int nextY = y + 1;
            boolean allMatch = true;
            for (BlockPos c : corners) {
                if (world.getBlockState(new BlockPos(c.getX(), nextY, c.getZ())).getBlock() != logBlock) {
                    allMatch = false;
                    break;
                }
            }
            if (!allMatch) break;
            y = nextY;
        }
        return y;
    }

    private static List<BlockPos> buildSmallColumnRejectingBranches(World world, BlockPos base, Block logBlock) throws MineTreeException {
        List<BlockPos> column = new ArrayList<>();
        column.add(base);
        BlockPos pos = base;
        for (int i = 0; i < MAX_TRUNK_SCAN; i++) {
            checkNoBranch(world, pos, logBlock);
            BlockPos above = pos.up();
            if (world.getBlockState(above).getBlock() == logBlock) {
                column.add(above);
                pos = above;
            } else {
                break;
            }
        }
        return column;
    }

    private static void checkNoBranch(World world, BlockPos trunkPos, Block logBlock) throws MineTreeException {
        BlockPos[] neighbors = {trunkPos.north(), trunkPos.south(), trunkPos.east(), trunkPos.west()};
        for (BlockPos n : neighbors) {
            if (world.getBlockState(n).getBlock() == logBlock) {
                throw new MineTreeException("This small tree has branching logs, which is not supported.");
            }
        }
    }
}
