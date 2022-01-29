package net.orandja.vw.mods

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ProtectBlock {

    companion object {

        // returns true if the player can destroy the block
        val DESTROY: ArrayList<(world: World, pos: BlockPos, entity: Entity) -> Boolean> = ArrayList()
        // returns true if the explosion can destroy the block
        val EXPLOSIONS: ArrayList<(world: World, pos: BlockPos) -> Boolean> = ArrayList()
        // returns false if the hopper can extract item from block **Might be changed in the future
        val EXTRACTORS: ArrayList<(world: World, pos: BlockPos) -> Boolean> = ArrayList()

        @JvmStatic
        fun canDestroy(world: World, pos: BlockPos, player: Any): Boolean {
            return DESTROY.find { protector -> protector.invoke(world, pos, player as Entity) } != null
        }

        @JvmStatic
        fun canExplode(world: World, pos: BlockPos): Boolean {
            return EXPLOSIONS.find { protector -> protector.invoke(world, pos) } != null
        }

        fun preventsExtract(world: World, pos: BlockPos): Boolean {
            return EXTRACTORS.find { extractor -> extractor.invoke(world, pos) } != null
        }

        @JvmStatic
        fun preventsExtract(world: World, x: Double, y: Double, z: Double): Boolean {
            return preventsExtract(world, BlockPos(x, y, z))
        }
    }
}