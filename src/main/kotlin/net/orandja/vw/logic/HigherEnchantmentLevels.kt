package net.orandja.vw.logic

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments

interface HigherEnchantmentLevels {

    companion object {

        val enchantmentsMaxLevels: Map<Enchantment, Int> = mapOf(
            Enchantments.EFFICIENCY to 10,
            Enchantments.MENDING to 5,
            Enchantments.UNBREAKING to 10,
        )

    }

    fun getMaxLevel(enchantment: Enchantment): Int {
        return enchantmentsMaxLevels[enchantment] ?: enchantment.maxLevel
    }
}