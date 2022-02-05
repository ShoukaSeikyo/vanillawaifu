package net.orandja.mcutils

import net.minecraft.block.*
import net.minecraft.tag.BlockTags

fun Block.isOre(): Boolean {
    return this is OreBlock || this is RedstoneOreBlock
}

fun Block.isOre(block: Block): Boolean {
    return this.isOre() && block == this
}

fun BlockState.isOre(): Boolean {
    return block.isOre()
}

fun BlockState.isOre(block: Block): Boolean {
    return this.block.isOre(block)
}

fun BlockState.isOre(state: BlockState): Boolean {
    return this.isOre(state.block)
}

fun BlockState.isWood(): Boolean {
    return isIn(BlockTags.LOGS) || material == Material.NETHER_WOOD
}

fun BlockState.isWood(state: BlockState): Boolean {
    return this.isWood() && state.block == this.block
}