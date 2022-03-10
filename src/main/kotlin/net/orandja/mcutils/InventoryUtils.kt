package net.orandja.mcutils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.CraftingScreenHandler
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.orandja.vw.accessor.CraftingInventoryAccessor
import net.orandja.vw.accessor.CraftingScreenHandlerAccessor
import net.orandja.vw.accessor.PlayerScreenHandlerAccessor
import net.orandja.vw.accessor.ScreenHandlerAccessor

fun CraftingInventory.getPlayer(): PlayerEntity? {
    val handler = this.getHandler()
    return ((handler as? CraftingScreenHandler) as? CraftingScreenHandlerAccessor)?.player ?: ((handler as? PlayerScreenHandler) as? PlayerScreenHandlerAccessor)?.owner
}

fun CraftingInventory.hasListeners(): Boolean {
    return (this.getHandler() as? ScreenHandlerAccessor)?.listeners?.isNotEmpty() ?: false
}

fun CraftingInventory.getHandler(): ScreenHandler? {
    return (this as CraftingInventoryAccessor).handler
}

fun CraftingInventory.gridStack(x: Int, y: Int): ItemStack {
    return this.getStack(gridIndex(x, y, this.width))
}

fun Slot.markDirtyIfEmpty(stack: ItemStack) {
    if (stack.isEmpty) {
        this.stack = ItemStack.EMPTY
    } else {
        markDirty()
    }
}

fun gridIndex(x: Int, y: Int, width: Int = 3): Int {
    return (y * width) + x
}

fun ItemStack.mergeInto(outputInventory: Inventory, stackProvider: () -> ItemStack = { this }): Boolean {
    for (index in 0 until outputInventory.size()) {
        if (outputInventory.isValid(index, this)) {
            val outputStack = outputInventory.getStack(index)
            if (outputStack.isEmpty) {
                outputInventory.setStack(index, stackProvider())
                outputInventory.markDirty()
                return true
            } else if (outputStack.canMerge(this, this.count)) {
                outputStack.increment(stackProvider().count)
                outputInventory.markDirty()
                return true
            }
        }
    }

    return false

}