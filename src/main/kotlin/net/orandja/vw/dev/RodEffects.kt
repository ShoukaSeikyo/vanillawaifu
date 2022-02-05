package net.orandja.vw.dev

import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

@Suppress("UNUSED_PARAMETER")
class RodEffects {

    companion object {

        val zones: HashMap<RegistryKey<World>, ArrayList<RodZone>> = HashMap()

        fun beforeLaunch() {

        }

        fun launch() {

        }
    }

    class RodZone(val start: Array<Int>, val end: Array<Int>, event: (world: World, pos: BlockPos) -> Unit) {

        fun isZone(pos: BlockPos): Boolean {
            return this.inLimit(start[0], pos.x, end[0]) && this.inLimit(start[1], pos.y, end[1]) && this.inLimit(start[2], pos.z, end[2])
        }

        fun inLimit(min: Int, pos: Int, max: Int): Boolean {
            return pos >= min && pos <= max
        }

    }

}