package net.orandja.vw.utils

import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.accessor.DefaultedListAccessor
import org.apache.commons.lang3.Validate
import java.util.*

fun ofSize(movingSize: Int, displaySize: Int, defaultEntry: ItemStack): MovingInventory {
    return MovingInventory(movingSize, displaySize, defaultEntry, arrayListOf(*Array<ItemStack>(movingSize) { ItemStack.EMPTY }))
}

class MovingInventory(
    val movingSize: Int,
    val displaySize: Int,
    val defaultEntry: ItemStack,
    val delegate: MutableList<ItemStack>
) : DefaultedList<ItemStack>(delegate, defaultEntry) {

    override fun set(index: Int, element: ItemStack): ItemStack {
        Validate.notNull(element)
        if(element.isEmpty) {
            delegate.removeAt(index)
            delegate.add(defaultEntry)
            return delegate.set(movingSize - 1, defaultEntry)
        } else {
            var iIndex = index
            while(iIndex > 0 && delegate[iIndex - 1].isEmpty) {
                iIndex--
            }
            return delegate.set(iIndex, element)
        }
    }
}