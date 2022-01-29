@file:Mixin(Enchantment::class)
@file:JvmName("EnchantmentMixin")

package net.orandja.vw.kmixin

import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.orandja.vw.mods.EnchantMore
import org.spongepowered.asm.mixin.Dynamic
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Enchantment::class)
abstract class EnchantmentMixin {

    @Inject(method = ["isAcceptableItem"], at = [At("HEAD")], cancellable = true)
    fun isAcceptableItem(stack: ItemStack, info: CallbackInfoReturnable<Boolean>) {
        if (EnchantMore.canEnchant(stack, this as Enchantment)) info.returnValue = true
    }
}