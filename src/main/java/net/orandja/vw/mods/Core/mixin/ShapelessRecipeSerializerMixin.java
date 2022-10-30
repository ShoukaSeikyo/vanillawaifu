package net.orandja.vw.mods.Core.mixin;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.orandja.vw.CustomRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ALL")
@Mixin(ShapelessRecipe.Serializer.class)
public abstract class ShapelessRecipeSerializerMixin implements RecipeSerializer<ShapelessRecipe>, CustomRecipe {

    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapelessRecipe;", at = @At(value = "RETURN"), cancellable = true)
    public void injectCustomRecipeJson(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<ShapelessRecipe> info) {
        if(hasCustomRecipe(identifier)) {
            info.setReturnValue(createFrom(info.getReturnValue()));
        }
    }

    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapelessRecipe;", at = @At(value = "RETURN"), cancellable = true)
    public void injectCustomRecipeBuffer(Identifier identifier, PacketByteBuf packetByteBuf, CallbackInfoReturnable<ShapelessRecipe> info) {
        if(hasCustomRecipe(identifier)) {
            info.setReturnValue(createFrom(info.getReturnValue()));
        }
    }

}
