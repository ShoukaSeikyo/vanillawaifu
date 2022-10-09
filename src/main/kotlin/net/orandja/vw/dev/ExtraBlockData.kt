@file:Suppress("unused")

package net.orandja.vw.dev

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

interface WorldChunkRegistryData {

    val blockData: Map<BlockPos, ExtraBlockData>
}

interface ExtraBlockData {
    fun toNBT(tag: NbtCompound)
    fun fromNBT(tag: NbtCompound)

}