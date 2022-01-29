@file:Mixin(PlayerEntity::class)
@file:JvmName("PlayerEntityMixin")

package net.orandja.vw.kmixin

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameMode
import net.minecraft.world.World
import net.orandja.vw.mods.ProtectBlock
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(PlayerEntity::class)
abstract class PlayerEntityMixin(entityType: EntityType<LivingEntity>, world: World) : LivingEntity(
    entityType,
    world
) {

    @Inject(at = [At("HEAD")], method = ["isBlockBreakingRestricted"], cancellable = true)
    fun isBlockBreakingRestricted( world: World, pos: BlockPos, gameMode: GameMode, info: CallbackInfoReturnable<Boolean> ) {
        if (!ProtectBlock.canDestroy(world, pos, this)) info.setReturnValue(true)
    }

}