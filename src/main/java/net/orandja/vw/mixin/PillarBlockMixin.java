package net.orandja.vw.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.DoubleTool;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unused")
@Mixin(PillarBlock.class)
public abstract class PillarBlockMixin extends Block implements DoubleTool {

    public PillarBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        if (useDoubleAxe(this, blockEntity, world, player, pos, state, stack)) {
            super.afterBreak(world, player, pos, state, blockEntity, stack);
        }
    }
}
