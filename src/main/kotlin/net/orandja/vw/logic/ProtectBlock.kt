package net.orandja.vw.logic

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

interface ProtectBlock {

    companion object {

        // returns true if the player can destroy the block
        val DESTROY: ArrayList<(world: World, pos: BlockPos, entity: Entity) -> Boolean> = ArrayList()
        // returns true if the explosion can destroy the block
        val EXPLOSION_PROTECTION: ArrayList<(world: World, pos: BlockPos) -> Boolean> = ArrayList()
        // returns false if the hopper can extract item from block **Might be changed in the future
        val EXTRACTION_PREVENTION: ArrayList<(world: World, pos: BlockPos) -> Boolean> = ArrayList()

    }

    fun preventsExtract(world: World, pos: BlockPos): Boolean {
        return EXTRACTION_PREVENTION.find { it(world, pos) } != null
    }

    fun preventsExtract(world: World, x: Double, y: Double, z: Double): Boolean {
        return preventsExtract(world, BlockPos(x, y, z))
    }

    val world: World?
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    var affectedBlocks: java.util.List<BlockPos>

    fun canExplode(world: World, pos: BlockPos): Boolean {
        return EXPLOSION_PROTECTION.find { it(world, pos) } == null
    }

    fun onExplosion() {
        val newBlocks = affectedBlocks.filter { world != null && canExplode(world!!, it) }.toList()
        affectedBlocks.clear()
        affectedBlocks.addAll(newBlocks)
    }

    fun canDestroy(world: World, pos: BlockPos, player: Any): Boolean {
        return DESTROY.find { it(world, pos, player as Entity) } != null
    }

    fun onPlayerDestroy(world: World, pos: BlockPos, player: Any, info: CallbackInfoReturnable<Boolean>) {
        if (!canDestroy(world, pos, this))
            info.returnValue = true;
    }
}