package net.orandja.vw.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.orandja.vw.logic.CustomRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("ALL")
@Mixin(ShapedRecipe.Serializer.class)
public abstract class ShapedRecipeSerializerMixin implements RecipeSerializer<ShapedRecipe>, CustomRecipe {

    @Redirect(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "NEW", target = "Lnet/minecraft/recipe/ShapedRecipe;"))
    public ShapedRecipe readJson(Identifier identifier, String group, int width, int height, DefaultedList input, ItemStack output) {
        return createShapedRecipe(identifier, group, width, height, input, output);
    }

    @Redirect(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "NEW", target = "Lnet/minecraft/recipe/ShapedRecipe;"))
    public ShapedRecipe readBuffer(Identifier identifier, String group, int width, int height, DefaultedList input, ItemStack output) {
        return createShapedRecipe(identifier, group, width, height, input, output);
    }

}
