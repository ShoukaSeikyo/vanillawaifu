@file:Mixin(CraftingInventory::class)
@file:JvmName("CraftingInventoryAccessor")
package net.orandja.vw.accessors

import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.recipe.RecipeInputProvider
import org.spongepowered.asm.mixin.Mixin

@Mixin(CraftingInventory::class)
interface CraftingInventoryAccessor: Inventory, RecipeInputProvider {
    @org.spongepowered.asm.mixin.gen.Accessor
    fun getHandler(): net.minecraft.screen.ScreenHandler
}