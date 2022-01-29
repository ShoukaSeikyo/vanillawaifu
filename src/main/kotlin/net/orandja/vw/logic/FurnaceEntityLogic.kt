package net.orandja.vw.logic

import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.orandja.vw.logic2.BlockWithEnchantment
import kotlin.math.max

interface FurnaceEntityLogic : BlockWithEnchantment {

    var smite: Short
    var efficiency: Short
    var flame: Short
    var unbreaking: Short
    var fireAspect: Short
    var fortune: Short

    var burnTime: Int
    var fuelTime: Int
    var cookTime: Int
    var cookTimeTotal: Int

    var inventory: DefaultedList<ItemStack>
    fun burning(): Boolean

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "smite" to smite,
            "flame" to flame,
            "unbreaking" to unbreaking,
            "fire_aspect" to fireAspect,
            "fortune" to fortune
        )
    }

    override fun hasEnchantments(): Boolean {
        return smite > 0 || efficiency > 0 || flame > 0 || unbreaking > 0 || fireAspect > 0 || fortune > 0
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "smite" -> smite = level
            "efficiency" -> efficiency = level
            "unbreaking" -> unbreaking = level
            "flame" -> flame = level
            "fire_aspect" -> fireAspect = level
            "fortune" -> fortune = level
        }
    }

    companion object {
        fun decreaseBurnTime(furnace: AbstractFurnaceBlockEntity) {
            if (furnace is FurnaceEntityLogic && (furnace.burning() && (furnace.flame == 0.toShort() || !furnace.inventory[0].isEmpty))) {
                furnace.burnTime = max(0, furnace.burnTime - max(1, (furnace.efficiency + furnace.smite) * 2))
            }
        }

        fun setBurnTime(furnace: AbstractFurnaceBlockEntity, burnTime: Int) {
            if (furnace is FurnaceEntityLogic)
                furnace.burnTime = burnTime + ((burnTime * (furnace.unbreaking + furnace.fireAspect)) / 5)
        }

        fun accelerateCookTime(furnace: AbstractFurnaceBlockEntity) {
            if (furnace is FurnaceEntityLogic && furnace.efficiency + furnace.smite > 0)
                furnace.cookTime = MathHelper.clamp(
                    furnace.cookTime - 1 + max(1, (furnace.efficiency + furnace.smite) * 2),
                    0,
                    furnace.cookTimeTotal
                )
        }

        fun increaseOutputAmount(furnace: AbstractFurnaceBlockEntity, world: World) {
            if (furnace is FurnaceEntityLogic && furnace.fortune > 0)
                furnace.inventory[2].increment(max(0, world.random.nextInt(furnace.fortune + 2) - 1))
        }
    }
}