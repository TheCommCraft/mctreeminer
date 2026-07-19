package com.minetree.util;

import com.minetree.MineTreeException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public final class ScaffoldBlocks {

    private ScaffoldBlocks() {
    }

    private static final Set<Block> WHITELIST = Set.of(Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.CHERRY_LOG, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.DARK_OAK_SAPLING);

    public static Item findOrGrant() throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) throw new MineTreeException("Player disconnected mid-task.");

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
            Block block = blockItem.getBlock();
            if (!WHITELIST.contains(block)) continue;
            if (!block.getDefaultState().isSolidBlock(player.getEntityWorld(), player.getBlockPos())) continue;
            return stack.getItem();
        }

        if (player.getAbilities().creativeMode) {
            return Blocks.DIRT.asItem();
        }

        throw new MineTreeException("No spare building blocks in your hotbar to scaffold with.");
    }
}
