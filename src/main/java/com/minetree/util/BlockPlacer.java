package com.minetree.util;

import com.minetree.MineTreeException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BlockPlacer {

    private BlockPlacer() {
    }

    public static void placeOn(BlockPos supportPos, Item item) throws MineTreeException {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null || mc.world == null) {
            throw new MineTreeException("Player disconnected mid-task.");
        }

        ensureCreativeItem(player, item);

        int slot = findHotbarSlot(player, item);
        if (slot < 0) {
            throw new MineTreeException("Need " + item.getTranslationKey() + " in your hotbar to continue.");
        }
        player.getInventory().setSelectedSlot(slot);

        BlockPos target = supportPos.up();
        if (!mc.world.getBlockState(target).isAir()) {
            return;
        }

        Vec3d hitPos = Vec3d.ofCenter(supportPos).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitPos, Direction.UP, supportPos, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);

        if (mc.world.getBlockState(target).isAir()) {
            throw new MineTreeException("Failed to place " + item.getTranslationKey() + " at " + target.toShortString() + ".");
        }
    }

    private static int findHotbarSlot(ClientPlayerEntity player, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private static void ensureCreativeItem(ClientPlayerEntity player, Item item) {
        if (!player.getAbilities().creativeMode) return;
        if (findHotbarSlot(player, item) >= 0) return;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                player.getInventory().setStack(i, new ItemStack(item, 64));
                return;
            }
        }
    }
}
