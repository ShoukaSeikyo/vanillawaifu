@file:Mixin(Explosion::class)
@file:JvmName("ExplosionMixin")

package net.orandja.vw.kmixin

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import net.orandja.vw.mods.ProtectBlock
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Explosion::class)
abstract class ExplosionMixin {
    @Shadow
    var world: World? = null

    @Shadow
    var affectedBlocks: List<BlockPos>? = null

    @Inject(method = ["affectWorld"], at = [At("HEAD")])
    fun affectWorld(particles: Boolean, info: CallbackInfo) {
        val newBlocks: List<BlockPos> = affectedBlocks!!.filterNot { pos: BlockPos ->
            ProtectBlock.canExplode(world!!, pos!!)
        }
        (this.affectedBlocks as ArrayList<BlockPos>).apply {
            this.clear()
            this.addAll(newBlocks)
        }
    }

}