package net.orandja.vw.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(net.minecraft.enchantment.LuckEnchantment.class)
public class LuckEnchantmentMixin {
    @Overwrite
	public int getMaxLevel() {
		//return Entry.getInstance().config.unbreaking;
		return 10;
	}
}
