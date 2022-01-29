package net.orandja.vanillawaifu.mixin.mobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.ai.goal.TemptItemGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaEntity.class)
public abstract class LlamaEntityMixin extends AbstractDonkeyEntity implements RangedAttackMob {
    protected LlamaEntityMixin(EntityType<? extends AbstractDonkeyEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("RETURN"))
    protected void initGoals(CallbackInfo info) {
        this.goalSelector.add(4, new TemptItemGoal(this, 1.25D, Ingredient.ofItems(Items.HAY_BLOCK), this::isTamed));
    }

    public boolean isTamed(AnimalEntity entity) {
        return entity instanceof LlamaEntity && ((LlamaEntity)entity).isTame();
    }
}
