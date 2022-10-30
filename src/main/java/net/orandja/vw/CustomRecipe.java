package net.orandja.vw;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;

public interface CustomRecipe {

    interface Interceptor {
        ItemStack onTakeItem(CraftingInventory input , PlayerEntity player, int slot, int amount);
    }

    interface ShapedRecipeConstructor {
        ShapedRecipe create(Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output);
    }

    interface ShapelessRecipeConstructor {
        ShapelessRecipe create(Identifier identifier, String group, ItemStack output, DefaultedList<Ingredient> input);
    }

    Map<Identifier, ShapedRecipeConstructor> customShapedRecipes = Maps.newHashMap();
    Map<Identifier, ShapelessRecipeConstructor> customShapelessRecipes = Maps.newHashMap();

    default boolean hasCustomRecipe(Identifier identifier) {
        return customShapelessRecipes.containsKey(identifier) || customShapedRecipes.containsKey(identifier);
    }

    default ShapedRecipe createShapedRecipe(Identifier identifier, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output) {
        if(customShapedRecipes.containsKey(identifier)) {
            return customShapedRecipes.get(identifier).create(identifier, group, width, height, input, output);
        }

        return null;
    }

    default ShapedRecipe createFrom(ShapedRecipe recipe) {
        return createShapedRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    default ShapelessRecipe createFrom(ShapelessRecipe recipe) {
        return createShapelessRecipe(recipe.getId(), recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
    }

    default ShapelessRecipe createShapelessRecipe(Identifier identifier, String group, ItemStack output, DefaultedList<Ingredient> input) {
        if(customShapelessRecipes.containsKey(identifier)) {
            return customShapelessRecipes.get(identifier).create(identifier, group, output, input);
        }

        return null;
    }

    default ItemStack interceptOnTakeItem(CraftingRecipe recipe, CraftingInventory input, PlayerEntity player, int slot, int amount) {
        if(recipe instanceof Interceptor interceptor) {
            return ((Interceptor) recipe).onTakeItem(input, player, slot, amount);
        }

        return input.removeStack(slot, amount);
    }

}
