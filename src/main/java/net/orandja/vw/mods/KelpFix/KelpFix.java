package net.orandja.vw.mods.KelpFix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicReference;

public interface KelpFix {

    Block getKelpPlant();

    default void afterKelpBreak(Block block, World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        if(world instanceof ServerWorld serverWorld) {
            Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, stack).forEach(itemStack -> {
                BlockPos upperPos = pos;
                while(isKelp(world, upperPos = upperPos.offset(Direction.Axis.Y, 1))) {
                    itemStack.increment(1);
                    world.setBlockState(upperPos, Blocks.WATER.getDefaultState(), 2);
                }
                Block.dropStack(world, pos, itemStack);
            });
            state.onStacksDropped(serverWorld, pos, stack, true);
        }
    }

    default boolean isKelp(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isOf(getKelpPlant()) || state.isOf(Blocks.KELP);
    }
}
