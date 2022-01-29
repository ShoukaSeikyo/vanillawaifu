@file:Mixin(AnimalEntity::class)
@file:JvmName("AnimalEntityMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.world.World
import net.orandja.vw.mods.AnimalTemptation
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(AnimalEntity::class)
abstract class AnimalEntityMixin(entityType: EntityType<out PassiveEntity>?, world: World?) : PassiveEntity(
    entityType,
    world
) {

    @Inject(
        at = [At("RETURN")],
        method = ["<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V"]
    )
    private fun init(entityType: EntityType<out PassiveEntity>?, world: World?, info: CallbackInfo) {
        if (world != null && !world.isClient) {
//            AnimalTemptation.affect(this) { pairGoal -> goalSelector.add(pairGoal.first, pairGoal.second) }
        }
    }
}