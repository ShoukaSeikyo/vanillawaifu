package net.orandja.mcutils

import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList

fun ItemStack.canMerge(other: ItemStack, countFromSecond: Int = other.count): Boolean {
    return (item === other.item) && (damage == other.damage) && ((count + countFromSecond) <= maxCount) && ItemStack.areNbtEqual(this, other)
}

fun ItemStack.isGonnaBreak(): Boolean {
    return isEmpty || damage >= maxDamage - 2
}

fun ItemStack.isCompatible(
    stack: ItemStack,
    checkSize: Boolean = false,
    allowEmpty: Boolean = true
): Boolean {
    return ((isEmpty || stack.isEmpty) && allowEmpty) ||
            (isItemEqual(stack) && ItemStack.areNbtEqual(this, stack) && (!checkSize || stack.count < stack.maxCount))
}

fun ItemStack.areCompatible(
    vararg stacks: ItemStack,
    checkSize: Boolean = false,
    allowEmpty: Boolean = true
): Boolean {
    return stacks.all { this.isCompatible(it, checkSize, allowEmpty) }
}

fun ItemStack.hasAnyEnchantments(): Boolean {
    if (this.nbt != null && this.nbt!!.contains("StoredEnchantments", 9)) {
        return !this.nbt!!.getList("StoredEnchantments", 10).isEmpty();
    }
    return this.hasEnchantments()
}

fun ItemStack.isSimilar(stack: ItemStack): Boolean {
    return this.isItemEqual(stack) && ItemStack.areNbtEqual(this, stack)
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


fun DefaultedList<ItemStack>.toNBT(nbt: NbtCompound, setIfEmpty: Boolean = false) {
    val list = filter { !it.isEmpty }.mapIndexed { slot, stack ->
        NbtCompound().apply {
            putShort("Slot", slot.toShort())
            stack.writeNbt(this)
        }
    }.toNBTList()

    if (!list.isEmpty() || setIfEmpty) {
        nbt.put("Items", list)
    }
}


fun DefaultedList<ItemStack>.fromNBT(nbt: NbtCompound) {
    nbt.getList("Items", 10).forEach {
        it as NbtCompound
        val slot = it.getShort("Slot").toInt()
        if((0..size).contains(slot))
            this[slot] = ItemStack.fromNbt(it)
    }
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