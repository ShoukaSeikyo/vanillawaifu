@file:Mixin(MobEntity::class)
@file:JvmName("MobEntityMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.GoalSelector
import net.minecraft.entity.mob.MobEntity
import net.minecraft.world.World
import net.orandja.vw.mods.AnimalTemptation
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(MobEntity::class)
abstract class MobEntityMixin(entityType: EntityType<out LivingEntity>?, world: World?) : LivingEntity(entityType,
    world
) {

    @field:Shadow
    var goalSelector: GoalSelector? = null

    @Inject(method = ["initGoals()V"], at = [At("RETURN")])
    open fun initGoals(info: CallbackInfo) {
//        AnimalTemptation.affect(this) { pairGoal -> goalSelector!!.add(pairGoal.first, pairGoal.second) }
    }
}