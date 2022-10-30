package net.orandja.vw.logic

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.orandja.vw.mods.BlockWithEnchantment.BlockWithEnchantment
import net.orandja.vw.mods.EnchantMore.EnchantMore
import net.orandja.vw.mods.ProtectBlock.ProtectBlock
import kotlin.math.max

interface EnchantedBrewingStand : BlockWithEnchantment {

    var brewTime: Int
    var fuel: Int
    var baneOfArthropods: Short
    var efficiency: Short
    var unbreaking: Short
    var fireAspect: Short
    var silkTouch: Short
    var inventory: DefaultedList<ItemStack>

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "bane_of_arthropods" to baneOfArthropods,
            "silk_touch" to silkTouch,
            "unbreaking" to unbreaking,
            "fire_aspect" to fireAspect
        )
    }

    override fun hasEnchantments(): Boolean {
        return baneOfArthropods > 0 || efficiency > 0 || unbreaking > 0 || fireAspect > 0 || silkTouch > 0
    }

    override fun applyEnchantment(name: String, level: Short) {
        when (name) {
            "bane_of_arthropods" -> baneOfArthropods = level
            "efficiency" -> efficiency = level
            "unbreaking" -> unbreaking = level
            "silk_touch" -> silkTouch = level
            "fire_aspect" -> fireAspect = level
        }
    }

    override fun hasEnchantment(enchantment: Enchantment, level: Short): Boolean {
        when (enchantment) {
            Enchantments.SILK_TOUCH -> return silkTouch >= level
            Enchantments.EFFICIENCY -> return efficiency >= level
            Enchantments.UNBREAKING -> return unbreaking >= level
            Enchantments.FIRE_ASPECT -> return fireAspect >= level
            Enchantments.BANE_OF_ARTHROPODS -> return baneOfArthropods >= level
        }

        return false
    }

    companion object {
        fun accelerate(stand: Any) {
            if (stand is EnchantedBrewingStand && (stand.efficiency + stand.baneOfArthropods > 0))
                stand.brewTime = MathHelper.clamp(
                    stand.brewTime + 1 - max(1, (stand.efficiency + stand.baneOfArthropods) * 2),
                    0,
                    400
                )
        }

        fun setFuelCount(stand: Any, fuel: Int) {
            if (stand is EnchantedBrewingStand)
                stand.fuel = fuel + fuel * (stand.unbreaking + stand.fireAspect) / 5
        }

        fun beforeLaunch() {
            EnchantMore.addBasic(
                Items.BREWING_STAND,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.EFFICIENCY,
                Enchantments.FIRE_ASPECT,
                Enchantments.UNBREAKING,
                Enchantments.SILK_TOUCH
            )

            ProtectBlock.EXTRACTION_PREVENTION.add(::preventExtractBrewingStand)
        }

        private fun preventExtractBrewingStand(world: World, pos: BlockPos): Boolean {
            val brewingStand = world.getBlockEntity(pos) as? EnchantedBrewingStand ?: return false
            return brewingStand.silkTouch > 0 && !brewingStand.inventory[3].isEmpty
        }
    }

}