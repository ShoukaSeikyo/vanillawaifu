package net.orandja.vw.mixin;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.orandja.vw.logic.CraftingDispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BlockWithEntity implements CraftingDispenserBlock {
    protected DispenserBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    void dispense(ServerWorld world, BlockPos pos, CallbackInfo info) {
        this.onBlockDispense(world, pos, info);
    }
}
