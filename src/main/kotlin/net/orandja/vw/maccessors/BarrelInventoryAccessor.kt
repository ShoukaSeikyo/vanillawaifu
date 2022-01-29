package net.orandja.vw.maccessors

import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

internal interface BarrelInventoryAccessor {
    fun getInventory(): DefaultedList<ItemStack>
}