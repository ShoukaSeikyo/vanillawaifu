package net.orandja.vanillawaifu.mixin.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.ai.goal.TemptItemGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity implements Angerable {
    protected WolfEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("RETURN"))
    protected void initGoals(CallbackInfo info) {
        this.goalSelector.add(9, new TemptItemGoal(this, 1.25D, this::isBreedingItem, this::isBreedeable));
    }

    public boolean isBreedeable(AnimalEntity entity) {
        return entity instanceof WolfEntity && ((WolfEntity)entity).isTamed() && entity.getHealth() == entity.getMaxHealth();
    }

    public boolean isBreedingItem(Item item) {
        return item.isFood() && item.getFoodComponent().isMeat();
    }
}
