package net.orandja.vw.mods.HigherEnchantmentLevel;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

import java.util.Map;

public interface HigherEnchantmentLevel {

    Map<Enchantment, Integer> maxLevels = Map.of(
        Enchantments.EFFICIENCY, 10,
        Enchantments.MENDING, 5,
        Enchantments.UNBREAKING, 10
    );

    default int getMaxLevel(Enchantment enchantment) {
        return maxLevels.containsKey(enchantment) ? maxLevels.get(enchantment) : enchantment.getMaxLevel();
    }

}
