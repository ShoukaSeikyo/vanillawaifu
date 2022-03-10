package net.orandja.vw.logic

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList

interface CustomRecipeInterceptor {

    fun onTakeItem(input: CraftingInventory, player: PlayerEntity, slot: Int, amount: Int): ItemStack

}

interface CustomRecipe {

    companion object {

        val customShapedRecipes: MutableMap<Identifier, (identifier: Identifier, group: String, width: Int, height: Int, input: DefaultedList<Ingredient>, output: ItemStack) -> ShapedRecipe?> = mutableMapOf()
        val customShapelessRecipes: MutableMap<Identifier, (identifier: Identifier, group: String, output: ItemStack, input: DefaultedList<Ingredient>) -> ShapelessRecipe?> = mutableMapOf()

    }

    fun createShapedRecipe(identifier: Identifier, group: String, width: Int, height: Int, input: DefaultedList<Ingredient>, output: ItemStack): ShapedRecipe {
        return customShapedRecipes[identifier]?.invoke(identifier, group, width, height, input, output) ?: ShapedRecipe(identifier, group, width, height, input, output)
    }

    fun createShapelessRecipe(identifier: Identifier, group: String, output: ItemStack, input: DefaultedList<Ingredient>): ShapelessRecipe {
        return customShapelessRecipes[identifier]?.invoke(identifier, group, output, input) ?: ShapelessRecipe(identifier, group, output, input)
    }

    fun interceptOnTakeItem(recipe: CraftingRecipe?, input: CraftingInventory, player: PlayerEntity, slot: Int, amount: Int): ItemStack {
        if(recipe is CustomRecipeInterceptor) {
            return recipe.onTakeItem(input, player, slot, amount)
        }

        return input.removeStack(slot, amount)
    }

}