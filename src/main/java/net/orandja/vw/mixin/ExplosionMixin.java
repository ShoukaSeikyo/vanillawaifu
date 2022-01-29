package net.orandja.vw.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.orandja.vw.mods.ProtectBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Explosion.class)
abstract class ExplosionMixin {
    @Shadow
    World world;

    @Shadow
    List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    void affectWorld(boolean particles, CallbackInfo info) {
        List<BlockPos> newBlocks  = affectedBlocks.stream().filter(pos -> ProtectBlock.canExplode(world, pos)).collect(Collectors.toList());
        this.affectedBlocks.clear();
        this.affectedBlocks.addAll(newBlocks);
    }

}
