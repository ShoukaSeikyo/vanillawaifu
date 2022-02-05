package net.orandja.vw.logic

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable


interface EnchantMore {
    companion object {
        var ENCHANTS: HashMap<Item, (Enchantment, ItemStack) -> Boolean> = HashMap()

        fun ItemStack.acceptsEnchant(enchantment: Enchantment): Boolean {
            return ENCHANTS[item]?.invoke(enchantment, this) ?: false
        }

        fun addBasic(item: Item, vararg enchantments: Enchantment, limit: Int = 1) {
            ENCHANTS[item] = { enchantment, stack -> stack.count <= limit && enchantments.contains(enchantment) }
        }

        fun addComplex(item: Item, complex: (Enchantment, ItemStack) -> Boolean) {
            ENCHANTS[item] = complex
        }
    }

    fun itemAcceptsEnchant(enchant: Any, stack: ItemStack, info: CallbackInfoReturnable<Boolean>) {
        if (stack.acceptsEnchant(enchant as Enchantment))
            info.returnValue = true
    }
}