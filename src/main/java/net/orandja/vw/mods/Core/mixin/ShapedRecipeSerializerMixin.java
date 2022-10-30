package net.orandja.vw.mods.Core.mixin;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.orandja.vw.CustomRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.Serializer.class)
public abstract class ShapedRecipeSerializerMixin implements RecipeSerializer<ShapedRecipe>, CustomRecipe {

    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "RETURN"), cancellable = true)
    public void injectCustomRecipeJson(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<ShapedRecipe> info) {
        if(hasCustomRecipe(identifier)) {
            info.setReturnValue(createFrom(info.getReturnValue()));
        }
    }


    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "RETURN"), cancellable = true)
    public void injectCustomRecipeBuffer(Identifier identifier, PacketByteBuf packetByteBuf, CallbackInfoReturnable<ShapedRecipe> info) {
        if(hasCustomRecipe(identifier)) {
            info.setReturnValue(createFrom(info.getReturnValue()));
        }
    }

}
