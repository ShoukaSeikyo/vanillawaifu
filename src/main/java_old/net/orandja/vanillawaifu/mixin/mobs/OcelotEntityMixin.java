package net.orandja.vanillawaifu.mixin.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.ai.goal.TemptItemGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OcelotEntity.class)
public abstract class OcelotEntityMixin extends AnimalEntity {
    protected OcelotEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("RETURN"))
    protected void initGoals(CallbackInfo info) {
        this.goalSelector.add(3, new TemptItemGoal(this, 1.25D, Ingredient.ofItems(Items.COD, Items.SALMON)));
    }
}
