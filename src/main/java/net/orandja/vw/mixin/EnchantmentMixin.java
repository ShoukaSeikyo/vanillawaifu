package net.orandja.vw.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.orandja.vw.mods.EnchantMore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.orandja.vw.utils.CatchUtilsKt.castAs;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin {

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        if (EnchantMore.canEnchant(stack, castAs(Enchantment.class, this))) info.setReturnValue(true);
    }
}
