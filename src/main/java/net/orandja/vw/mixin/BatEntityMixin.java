package net.orandja.vw.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatEntity.class)
public abstract class BatEntityMixin extends AmbientEntity {
    protected BatEntityMixin(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
    }

    public short mobTickCooldown = 30;

    @Inject(at = @At("HEAD"), method = "mobTick", cancellable = true)
    protected void mobTick(CallbackInfo callbackInfo) {
        mobTickCooldown--;
        if(mobTickCooldown > 0) {
            callbackInfo.cancel();
            return;
        }

        mobTickCooldown = 30;
    }
}