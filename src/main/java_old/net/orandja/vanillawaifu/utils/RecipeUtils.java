package net.orandja.vanillawaifu.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class RecipeUtils {

    private static MinecraftServer server;
    private static Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;
    private static Field recipeListField;

    public static void init(MinecraftServer server) {
        RecipeUtils.server = server;
        recipeListField = QuickUtils.quickField(RecipeManager.class, Map.class);
        recipes = QuickUtils.quickGet(recipeListField, server.getRecipeManager());
    }

    public static void addRecipe(Recipe<?> recipe) {
        try {
            addRecipe_(recipe);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void addRecipe_(Recipe<?> newRecipe) throws IllegalAccessException {
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> newRecipes = Maps.newHashMap();
        if (newRecipes.computeIfAbsent(newRecipe.getType(), type -> Maps.newHashMap()).put(newRecipe.getId(), newRecipe) != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + newRecipe.getId());
        }

        recipes.values().forEach(recipeMap -> {
            recipeMap.values().forEach(recipe -> {
                if (newRecipes.computeIfAbsent(recipe.getType(), type -> Maps.newHashMap()).put(recipe.getId(), recipe) != null) {
                    throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
                }
            });
        });

        recipeListField.set(server.getRecipeManager(), ImmutableMap.copyOf(newRecipes));
        recipes = newRecipes;
    }

    public static <T> T createObject(T tag, Consumer<T> consumer) {
        consumer.accept(tag);
        return tag;
    }

    public static DefaultedList<Ingredient> getIngredients(String[] pattern, Map<String, Ingredient> key, int width, int height) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(key.keySet());
        set.remove(" ");

        for (int i = 0; i < pattern.length; ++i) {
            for (int j = 0; j < pattern[i].length(); ++j) {
                String string = pattern[i].substring(j, j + 1);
                Ingredient ingredient = key.get(string);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
                }

                set.remove(string);
                defaultedList.set(j + width * i, ingredient);
            }
        }

        if (!set.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        } else {
            return defaultedList;
        }
    }


}
