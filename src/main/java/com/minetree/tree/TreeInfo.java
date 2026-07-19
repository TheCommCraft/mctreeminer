package com.minetree.tree;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class TreeInfo {

    public final TreeType type;
    public final Block logBlock;
    public final Block saplingBlock;

    public final List<BlockPos> stemColumn;

    public final List<BlockPos> stemBaseCorners;

    public final int baseY;
    public final int topY;

    private TreeInfo(TreeType type, Block logBlock, Block saplingBlock, List<BlockPos> stemColumn, List<BlockPos> stemBaseCorners, int baseY, int topY) {
        this.type = type;
        this.logBlock = logBlock;
        this.saplingBlock = saplingBlock;
        this.stemColumn = stemColumn;
        this.stemBaseCorners = stemBaseCorners;
        this.baseY = baseY;
        this.topY = topY;
    }

    public static TreeInfo small(Block logBlock, Block saplingBlock, List<BlockPos> column) {
        int baseY = column.get(0).getY();
        int topY = column.get(column.size() - 1).getY();
        return new TreeInfo(TreeType.SMALL, logBlock, saplingBlock, column, null, baseY, topY);
    }

    public static TreeInfo large(Block logBlock, Block saplingBlock, List<BlockPos> baseCorners, int baseY, int topY) {
        return new TreeInfo(TreeType.LARGE, logBlock, saplingBlock, null, baseCorners, baseY, topY);
    }
}
