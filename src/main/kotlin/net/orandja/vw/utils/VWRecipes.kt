package net.orandja.vw.utils

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.VW
import net.orandja.vw.accessor.RecipeManagerAccessor


class VWRecipes {
    companion object {
        fun getRecipes(): Map<RecipeType<*>, Map<Identifier, Recipe<*>>> {
            return (VW.server.recipeManager as RecipeManagerAccessor).recipes
        }

        fun setRecipes(recipes: Map<RecipeType<*>, Map<Identifier, Recipe<*>>>) {
            (VW.server.recipeManager as RecipeManagerAccessor).recipes = recipes
        }

        fun addRecipe(newRecipe: Recipe<*>) {
            val newRecipes: MutableMap<RecipeType<*>, MutableMap<Identifier, Recipe<*>>> = Maps.newHashMap()
            check(
                newRecipes.computeIfAbsent(newRecipe.type) { type: RecipeType<*>? -> Maps.newHashMap() }
                    .put(newRecipe.id, newRecipe) == null
            ) { "Duplicate recipe ignored with ID " + newRecipe.id }
            getRecipes().values.forEach { recipeMap: Map<Identifier, Recipe<*>> ->
                recipeMap.values.forEach { recipe: Recipe<*> ->
                    check(newRecipes.computeIfAbsent(recipe.type) { type: RecipeType<*>? -> Maps.newHashMap() }
                        .put(recipe.id, recipe) == null) {
                        "Duplicate recipe ignored with ID " + recipe.id
                    }
                }
            }

            setRecipes(newRecipes)
        }

        fun getIngredients(
            pattern: Array<String>,
            key: Map<String, Ingredient>,
            width: Int,
            height: Int
        ): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.ofSize(width * height, Ingredient.EMPTY)
            val set: MutableSet<String> = Sets.newHashSet(key.keys)
            set.remove(" ")
            for (i in pattern.indices) {
                for (j in 0 until pattern[i].length) {
                    val string = pattern[i].substring(j, j + 1)
                    val ingredient = key[string]
                        ?: throw JsonSyntaxException("Pattern references symbol '$string' but it's not defined in the key")
                    set.remove(string)
                    defaultedList[j + width * i] = ingredient
                }
            }
            return if (set.isNotEmpty()) {
                throw JsonSyntaxException("Key defines symbols that aren't used in pattern: $set")
            } else {
                defaultedList
            }
        }

        fun getShapelessIngredients(vararg ingredients: Ingredient): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.ofSize(ingredients.size, Ingredient.EMPTY)
            ingredients.forEach(defaultedList::add)
            return defaultedList
        }

        fun ingredient(name: String): Ingredient {
            return Ingredient.fromJson(JsonParser().parse("""{"item":"minecraft:${name}"}"""))
        }
    }
}