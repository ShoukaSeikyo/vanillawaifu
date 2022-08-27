package net.orandja.vw.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin extends PlantBlock {

    protected CropBlockMixin(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, stack, dropExperience);
        if(dropExperience)
            ExperienceOrbEntity.spawn(world, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), 1);
    }

}
