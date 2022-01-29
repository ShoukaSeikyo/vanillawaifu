package net.orandja.vanillawaifu.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.orandja.vanillawaifu.mods.ProtectBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    private World world;

    @Shadow
    private List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    public void affectWorld(boolean bl, CallbackInfo info) {
        this.affectedBlocks = this.affectedBlocks.stream().filter(pos -> ProtectBlock.canDestroy(world, pos, this)).collect(Collectors.toList());
    }
}
