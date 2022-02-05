package net.orandja.mcutils

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.util.math.BlockPos
import net.orandja.vw.logic.DoubleTool
import net.orandja.vw.logic.DoubleToolMode

fun PlayerEntity.areBothToolsSuitable(state: BlockState, clazz: Class<*>): Boolean {
    if (clazz.isInstance(mainHandStack.item) && clazz.isInstance(offHandStack.item)) {
        return (mainHandStack.item as ToolItem).material == (offHandStack.item as ToolItem).material && mainHandStack.isSuitableFor(
            state
        )
    }
    return false
}

fun PlayerEntity.areBothTools(clazz: Class<*>): Boolean {
    return clazz.isInstance(mainHandStack.item) && clazz.isInstance(offHandStack.item) && (mainHandStack.item as ToolItem).material == (offHandStack.item as ToolItem).material
}

fun PlayerEntity.getToolMode(): DoubleToolMode {
    return (this as DoubleTool).getToolMode(this.mainHandStack)
}

fun PlayerEntity.getBothTools(): Pair<ItemStack, ItemStack> {
    return Pair(mainHandStack, offHandStack)
}

fun PlayerEntity.tryBreakBlock(pos: BlockPos) {
    val blockEntity = world.getBlockEntity(pos)
    val state = world.getBlockState(pos)
    val block = state.block
    if (world.breakBlock(pos, false)) {
        block.onBroken(world, pos, state)
        if (canHarvest(state)) {
            block.afterBreak(world, this, pos, state, blockEntity, mainHandStack.copy())
        }
    }
}

fun PlayerEntity.anyToolBreaking(): Boolean {
    return this.mainHandStack.isGonnaBreak() || this.offHandStack.isGonnaBreak()
}