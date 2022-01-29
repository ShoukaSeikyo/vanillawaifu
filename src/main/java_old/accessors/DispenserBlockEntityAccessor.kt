package net.orandja.vw.accessors

import net.minecraft.block.entity.DispenserBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(DispenserBlockEntity::class)
interface DispenserBlockEntityAccessor {

    @Accessor
    fun getInventory(): DefaultedList<ItemStack>
}