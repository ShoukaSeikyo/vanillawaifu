package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.orandja.vw.logic.ProtectBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
abstract class ExplosionMixin implements ProtectBlock {
    @Shadow @Getter World world;

    @Shadow @Getter @Setter List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    void affectWorld(boolean particles, CallbackInfo info) {
        onExplosion();
    }

}
