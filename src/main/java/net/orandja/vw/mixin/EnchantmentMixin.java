package net.orandja.vw.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.orandja.vw.logic.EnchantMore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin implements EnchantMore {

    @Shadow public abstract boolean isAcceptableItem(ItemStack stack);

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        itemAcceptsEnchant(this, stack, info);
    }
}
