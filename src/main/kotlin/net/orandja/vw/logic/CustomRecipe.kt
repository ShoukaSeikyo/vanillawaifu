package net.orandja.vw.logic

import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList

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

}