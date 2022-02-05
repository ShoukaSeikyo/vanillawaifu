package net.orandja.mcutils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
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

fun Slot.markDirtyIfEmpty(stack: ItemStack) {
    if (stack.isEmpty) {
        this.stack = ItemStack.EMPTY
    } else {
        markDirty()
    }
}