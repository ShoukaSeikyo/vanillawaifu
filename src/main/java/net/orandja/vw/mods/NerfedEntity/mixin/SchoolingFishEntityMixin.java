package net.orandja.vw.mods.NerfedEntity.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.world.World;
import net.orandja.vw.mods.NerfedEntity.NerfedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SchoolingFishEntity.class)
public abstract class SchoolingFishEntityMixin extends FishEntity implements NerfedEntity {
    public SchoolingFishEntityMixin(EntityType<? extends FishEntity> entityType, World world) {
        super(entityType, world);
    }

    @Getter @Setter public short tickCooldown = 30;

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo info) {
        nerfAI(info);
    }

    @Override
    public boolean isMobDead() {
        return this.isDead();
    }
}
