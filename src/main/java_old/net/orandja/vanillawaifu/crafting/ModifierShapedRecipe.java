package net.orandja.vanillawaifu.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.utils.QuickUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static net.orandja.vanillawaifu.utils.QuickUtils.noCatch;
import static net.orandja.vanillawaifu.utils.QuickUtils.quickField;
import static net.orandja.vanillawaifu.utils.RecipeUtils.createObject;

public class ModifierShapedRecipe extends ShapedRecipe {

    public interface ModifierRecipeConsumer {
        ItemStack craft(CraftingInventory inventory, PlayerEntity player, ItemStack normalOutput);
    }

    public static Field handlerField = quickField(CraftingInventory.class, field -> field.getType().equals(ScreenHandler.class));
    public static Field listenerField = quickField(ScreenHandler.class, field -> {
        if (field.getGenericType() instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return types.length == 1 && types[0].equals(ScreenHandlerListener.class);
        }

        return false;
    });

    ModifierRecipeConsumer consumer;

    public ModifierShapedRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack output) {
        super(id, group, width, height, ingredients, output);
    }

    public ModifierShapedRecipe setConsumer(ModifierRecipeConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public boolean matches(CraftingInventory craftingInventory, World world) {
        return noCatch(false, () -> matches_(craftingInventory, world));
    }

    public boolean matches_(CraftingInventory craftingInventory, World world) throws IllegalAccessException {
        List<ScreenHandlerListener> listeners = (List<ScreenHandlerListener>) listenerField.get(handlerField.get(craftingInventory));
        if(listeners.size() > 0 && listeners.get(0) instanceof PlayerEntity) {
            return super.matches(craftingInventory, world);
        }

        return false;
    }

    public ItemStack craft(CraftingInventory craftingInventory) {
        return noCatch(this.getOutput().copy(), () -> craft_(craftingInventory));
    }

    public ItemStack craft_(CraftingInventory craftingInventory) throws IllegalAccessException {
        ItemStack itemStack = this.getOutput().copy();
        List<ScreenHandlerListener> listeners = (List<ScreenHandlerListener>) listenerField.get(handlerField.get(craftingInventory));

        if (listeners.size() > 0 && listeners.get(0) instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) listeners.get(0);

            if(this.consumer != null) {
                return this.consumer.craft(craftingInventory, player, itemStack);
            }
        }


        return itemStack;
    }
}