package net.orandja.mcutils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.orandja.vw.logic.DoubleTool;
import net.orandja.vw.logic.DoubleToolMode;

public abstract class PlayerUtils {

    public static boolean areBothToolsSuitable(PlayerEntity player, BlockState state, Class<?> clazz) {
        return areBothTools(player, clazz) && player.getMainHandStack().isSuitableFor(state);
    }

    public static boolean areBothTools(PlayerEntity player, Class<?> clazz) {
        return clazz.isInstance(player.getMainHandStack().getItem()) && clazz.isInstance(player.getOffHandStack().getItem()) && StackUtils.getToolMaterial(player.getMainHandStack()) == StackUtils.getToolMaterial(player.getOffHandStack());
    }

    public static DoubleToolMode getToolMode(PlayerEntity player) {
        return ((DoubleTool)player).getToolMode(player.getMainHandStack());
    }

    public static void tryBreakBlock(PlayerEntity player, BlockPos pos) {
        BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
        BlockState state = player.getWorld().getBlockState(pos);
        Block block = state.getBlock();
        if(player.getWorld().breakBlock(pos, false)) {
            block.onBroken(player.getWorld(), pos, state);
            if(player.canHarvest(state)) {
                block.afterBreak(player.getWorld(), player, pos, state, blockEntity, player.getMainHandStack().copy());
            }
        }
    }

    public static boolean anyToolBreaking(PlayerEntity player) {
        return StackUtils.isGonnaBreak(player.getMainHandStack()) || StackUtils.isGonnaBreak(player.getOffHandStack());
    }

}
