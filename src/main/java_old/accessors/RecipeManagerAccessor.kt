@file:Mixin(RecipeManager::class)
@file:JvmName("RecipeManagerAccessor")
package net.orandja.vw.accessors

import com.google.gson.Gson
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.resource.JsonDataLoader
import net.minecraft.util.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(RecipeManager::class)
interface RecipeManagerAccessor {
    @Accessor("recipes")
    fun setRecipes(recipes: Map<RecipeType<*>, Map<Identifier, Recipe<*>>>)

    @Accessor
    fun getRecipes(): Map<RecipeType<*>, Map<Identifier, Recipe<*>>>
}