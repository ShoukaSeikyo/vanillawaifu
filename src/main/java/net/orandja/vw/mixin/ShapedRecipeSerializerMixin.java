//package net.orandja.vw.mixin;
//
//import com.google.gson.JsonObject;
//import net.minecraft.network.PacketByteBuf;
//import net.minecraft.recipe.RecipeSerializer;
//import net.minecraft.recipe.ShapedRecipe;
//import net.minecraft.util.Identifier;
//import net.orandja.vw.CustomRecipe;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(ShapedRecipe.Serializer.class)
//public abstract class ShapedRecipeSerializerMixin implements RecipeSerializer<ShapedRecipe>, CustomRecipe {
//
//    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "RETURN"), cancellable = true)
//    public void injectCustomRecipeJson(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable<ShapedRecipe> info) {
//        if(hasCustomRecipe(identifier)) {
//            info.setReturnValue(createFrom(info.getReturnValue()));
//        }
//    }
//
//
//    @Inject(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "RETURN"), cancellable = true)
//    public void injectCustomRecipeBuffer(Identifier identifier, PacketByteBuf packetByteBuf, CallbackInfoReturnable<ShapedRecipe> info) {
//        if(hasCustomRecipe(identifier)) {
//            info.setReturnValue(createFrom(info.getReturnValue()));
//        }
//    }
//
////    @SuppressWarnings("ALL")
////    @Redirect(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "NEW", target = "Lnet/minecraft/recipe/ShapedRecipe;"))
////    public ShapedRecipe readJson(Identifier identifier, String group, int width, int height, DefaultedList input, ItemStack output) {
////        return createShapedRecipe(identifier, group, width, height, input, output);
////    }
////
////    @SuppressWarnings("ALL")
////    @Redirect(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At(value = "NEW", target = "Lnet/minecraft/recipe/ShapedRecipe;"))
////    public ShapedRecipe readBuffer(Identifier identifier, String group, int width, int height, DefaultedList input, ItemStack output) {
////        return createShapedRecipe(identifier, group, width, height, input, output);
////    }
//
//
////    @ModifyVariable(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At("STORE"))
////    private ItemStack injected(ItemStack stack, Identifier identifier, JsonObject object) {
////        return HolyWrench.Companion.hackRecipeOutput(identifier, stack);
////    }
////
////
////    @ModifyVariable(method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/ShapedRecipe;", at = @At("STORE"))
////    private ItemStack injected(ItemStack stack, Identifier identifier, PacketByteBuf object) {
////        return HolyWrench.Companion.hackRecipeOutput(identifier, stack);
////    }
//
//}
