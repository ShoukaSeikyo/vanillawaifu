package net.orandja.vw.crafting

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import net.orandja.vw.accessor.CraftingInventoryAccessor
import net.orandja.vw.accessor.ScreenHandlerAccessor
import net.orandja.vw.utils.catchNone
import net.orandja.vw.utils.getPlayer
import net.orandja.vw.utils.notNull
import net.orandja.vw.utils.toArray

class ComplexShapelessRecipe(
    id: Identifier, group: String, output: ItemStack,
    private val input: DefaultedList<Ingredient>,
    private val matchModifier: (CraftingInventory, PlayerEntity) -> Boolean,
    private val outputModifier: (CraftingInventory, PlayerEntity, ItemStack) -> ItemStack
) : ShapelessRecipe(id, group, output, input) {

    override fun matches(inventory: CraftingInventory?, world: World): Boolean {
        return inventory != null && catchNone(faultValue = false, debug = true) { matches_(inventory, world) }
    }

    override fun craft(inventory: CraftingInventory): ItemStack {
        return catchNone(this.output.copy(), true) { craft_(inventory) }
    }

    private fun matches_(inventory: CraftingInventory, world: World): Boolean {
        val recipeMatcher = RecipeMatcher()

        val list = inventory.toArray(true)
        list.forEach { stack -> recipeMatcher.addInput(stack, 1) }
        val baseFlag = list.size == input.size && recipeMatcher.match(this, null)

        val hasListeners = ((inventory as CraftingInventoryAccessor).handler as ScreenHandlerAccessor).listeners.isNotEmpty()
        val player: PlayerEntity? = inventory.getPlayer()!!
        return baseFlag && hasListeners && notNull(player) && matchModifier.invoke(inventory, player!!)
    }

    private fun craft_(inventory: CraftingInventory): ItemStack {
        val hasListeners = ((inventory as CraftingInventoryAccessor).handler as ScreenHandlerAccessor).listeners.isNotEmpty()
        val player: PlayerEntity = inventory.getPlayer()!!
        return if (hasListeners && notNull(player)) outputModifier.invoke(inventory, player, output.copy()) else output.copy()
    }
}