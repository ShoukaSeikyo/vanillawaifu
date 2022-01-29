package net.orandja.vw.crafting

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.util.collection.DefaultedList

class AutomatedCraftingInventory(width: Int, height: Int, stacks: List<ItemStack>) :
    CraftingInventory(null, width, height) {
    private val stacks: DefaultedList<ItemStack>
    private val width: Int
    private val height: Int
    override fun size(): Int {
        return this.stacks.size
    }

    override fun isEmpty(): Boolean {
        val var1: Iterator<*> = this.stacks.iterator()
        var itemStack: ItemStack
        do {
            if (!var1.hasNext()) {
                return true
            }
            itemStack = var1.next() as ItemStack
        } while (itemStack.isEmpty)
        return false
    }

    override fun getStack(slot: Int): ItemStack {
        return if (slot >= size()) ItemStack.EMPTY else this.stacks[slot]
    }

    override fun removeStack(slot: Int): ItemStack {
        return Inventories.removeStack(this.stacks, slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return Inventories.splitStack(this.stacks, slot, amount)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        this.stacks[slot] = stack
    }

    override fun markDirty() {}
    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun clear() {
        this.stacks.clear()
    }

    override fun getHeight(): Int {
        return this.height
    }

    override fun getWidth(): Int {
        return this.width
    }

    override fun provideRecipeInputs(finder: RecipeMatcher) {
        val var2: Iterator<*> = this.stacks.iterator()
        while (var2.hasNext()) {
            val itemStack = var2.next() as ItemStack
            finder.addInput(itemStack)
        }
    }

    init {
        this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY)
        for (i in stacks.indices) {
            this.stacks[i] = stacks[i]
        }
        this.width = width
        this.height = height
    }
}