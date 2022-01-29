package net.orandja.vw.crafting

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.orandja.vw.utils.areStacksCompatible
import net.orandja.vw.utils.grid
import net.orandja.vw.utils.markDirtyIfEmpty

class DeepStorageSlot(inventory: Inventory, val inventoryIndex: Int, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean {
        return areStacksCompatible(this.inventory.getStack(0), stack)
    }

    override fun getStack(): ItemStack {
        return inventory.getStack(inventoryIndex)
    }

    override fun setStack(stack: ItemStack) {
        inventory.setStack(inventoryIndex, stack)
        markDirty()
    }

    override fun takeStack(amount: Int): ItemStack {
        return inventory.removeStack(inventoryIndex, amount)
    }
}

class DeepStorageScreenHandler(syncId: Int, playerInventory: PlayerInventory, val inventory: Inventory) : ScreenHandler(ScreenHandlerType.SHULKER_BOX, syncId) {

    override fun canUse(player: PlayerEntity): Boolean {
        return inventory.canPlayerUse(player)
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY
        }

        val itemStack2 = slot.stack
        val itemStack = itemStack2.copy()
        if (index < 27) {
            if (!insertItem(itemStack2, 27, slots.size, true)) {
                return ItemStack.EMPTY
            }
        } else if (!insertItem(itemStack2, 0, 27, false)) {
            return ItemStack.EMPTY
        }

        slot.markDirtyIfEmpty(itemStack2)

        return itemStack
    }

    override fun close(player: PlayerEntity) {
        super.close(player)
        inventory.onClose(player)
    }

    init {
        checkSize(inventory, 27)
        inventory.onOpen(playerInventory.player)

        grid(9, 2) { x, y -> addSlot(DeepStorageSlot(inventory, x + (y * 9), x + (y * 9), 8 + x * 18, 18 + y * 18)) }
        grid(9) { x, _ -> addSlot(DeepStorageSlot(inventory, (inventory.size() - 9) + x, x + 18, 8 + x * 18, 18 + (2 * 18))) }
        grid(9, 3) { x, y -> addSlot(Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18)) }
        grid(9) { x, _ -> addSlot(Slot(playerInventory, x, 8 + x * 18, 142)) }
    }
}