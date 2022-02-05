package net.orandja.mcutils

import net.minecraft.util.math.BlockPos

operator fun BlockPos.plus(pos: Array<Int>): BlockPos {
    return this.add(pos[0], pos[1], pos[2])
}

fun grid(width: Int, height: Int = 1, gridConsumer: (x: Int, y: Int) -> Unit) {
    for(y in 0 until height) {
        for(x in 0 until width) {
            gridConsumer.invoke(x, y)
        }
    }
}