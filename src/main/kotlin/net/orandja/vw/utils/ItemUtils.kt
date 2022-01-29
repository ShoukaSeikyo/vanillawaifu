package net.orandja.vw.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.screen.CraftingScreenHandler
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.accessor.CraftingInventoryAccessor
import net.orandja.vw.accessor.CraftingScreenHandlerAccessor
import net.orandja.vw.accessor.PlayerScreenHandlerAccessor
import net.orandja.vw.accessor.ScreenHandlerAccessor

fun areStacksCompatible(a: ItemStack, b: ItemStack): Boolean {
    return a.isEmpty || b.isEmpty || (a.isItemEqual(b) && ItemStack.areNbtEqual(a, b))
}

fun canMergeItems(first: ItemStack, second: ItemStack, countFromSecond: Int = second.count): Boolean {
    return (first.item === second.item) && (first.damage == second.damage) && ((first.count + countFromSecond) <= first.maxCount) && ItemStack.areNbtEqual(first, second)
}

fun Inventory.toArray(nonEmpty: Boolean = false): Array<ItemStack> {
    val list = ArrayList<ItemStack>()
    for (j in 0 until size()) {
        val itemStack = getStack(j)
        if (!nonEmpty || !itemStack.isEmpty) {
            list.add(itemStack)
        }
    }

    return Array(list.size, list::get)
}

fun DefaultedList<ItemStack>.wholeCount(): Int {
    var count: Int = 0
    for (j in 0 until size) {
        val itemStack = get(j)
        if (!itemStack.isEmpty) {
            count += itemStack.count
        }
    }

    return count;
}

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

fun ItemStack.getTagOrCreate(): NbtCompound {
    this.nbt = this.nbt ?: NbtCompound()
    return this.nbt!!
}

fun ItemStack.computeTag(compute: (NbtCompound) -> Unit) {
    compute.invoke(getTagOrCreate())
}

fun ItemStack.isOfItem(item: Item): Boolean {
    return !this.isEmpty && this.item === item
}

fun ItemStack.isOfItem(stack: ItemStack): Boolean {
    return isOfItem(stack.item)
}

fun Item.ofStack(stack: ItemStack): Boolean {
    return !stack.isEmpty && this === stack.item
}

fun grid(width: Int, height: Int = 1, gridConsumer: (x: Int, y: Int) -> Unit) {
    for(y in 0 until height) {
        for(x in 0 until width) {
            gridConsumer.invoke(x, y)
        }
    }
}

fun Slot.markDirtyIfEmpty(stack: ItemStack) {
    if (stack.isEmpty) {
        this.stack = ItemStack.EMPTY
    } else {
        markDirty()
    }
}

fun writeNbt(nbt: NbtCompound, stacks: DefaultedList<ItemStack>): NbtCompound {
    return writeNbt(nbt, stacks, true)
}

fun writeNbt(nbt: NbtCompound, stacks: DefaultedList<ItemStack>, setIfEmpty: Boolean): NbtCompound {
    val nbtList = NbtList()
    for (i in stacks.indices) {
        val itemStack = stacks[i]
        if (!itemStack.isEmpty) {
            val nbtCompound = NbtCompound()
            nbtCompound.putShort("Slot", i.toShort())
            itemStack.writeNbt(nbtCompound)
            nbtList.add(nbtCompound)
        }
    }
    if (!nbtList.isEmpty() || setIfEmpty) {
        nbt.put("Items", nbtList)
    }
    return nbt
}

fun readNbt(nbt: NbtCompound, stacks: DefaultedList<ItemStack>) {
    val nbtList = nbt.getList("Items", 10)
    for (i in nbtList.indices) {
        val tag = nbtList.getCompound(i)
        val j: Short = tag.getShort("Slot")
        if (j >= 0 && j < stacks.size) {
            stacks[j.toInt()] = ItemStack.fromNbt(tag)
        }
    }
}