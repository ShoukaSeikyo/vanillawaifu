package net.orandja.vw.crafting

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import net.orandja.vw.utils.*

class ModifierShapedRecipe(
    id: Identifier, group: String, width: Int, height: Int, ingredients: DefaultedList<Ingredient>, output: ItemStack,
    var modifier: (CraftingInventory, PlayerEntity, ItemStack) -> ItemStack
) : ShapedRecipe(id, group, width, height, ingredients, output) {

    override fun matches(inventory: CraftingInventory, world: World): Boolean {
        return inventory != null && catchNone(faultValue = false, debug = true) { matches_(inventory, world) }
    }

    override fun craft(inventory: CraftingInventory): ItemStack {
        return catchNone(this.output.copy(), true) { craft_(inventory) }
    }

    private fun matches_(inventory: CraftingInventory, world: World): Boolean {
        return inventory.hasListeners() && notNull(inventory.getPlayer()) && super.matches(inventory, world)
    }

    private fun craft_(inventory: CraftingInventory): ItemStack {
        val player = inventory.getPlayer()!!
        return if (inventory.hasListeners() && notNull(player))
            modifier.invoke(inventory, player, output.copy())
        else
            output.copy();
    }
}