package net.orandja.vw.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.orandja.vw.logic.KelpPlantLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KelpPlantBlock.class)
public abstract class KelpPlantBlockMixin extends AbstractPlantBlock implements FluidFillable, KelpPlantLogic {
    protected KelpPlantBlockMixin(Settings settings, Direction direction, VoxelShape voxelShape, boolean bl) {
        super(settings, direction, voxelShape, bl);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        afterBreak(this, world, player, pos, state, blockEntity, stack);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            afterBreak(this, world, null, pos, state, null, ItemStack.EMPTY);
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
        }
    }

    public @NotNull Block getKelpPlant() {
        return getPlant();
    }
}
