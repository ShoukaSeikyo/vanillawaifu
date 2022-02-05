package net.orandja.vw.logic

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BucketItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.orandja.vw.logic.EnchantMore.Companion.addBasic

fun PlayerEntity.slotMainOrOffHand(stack: ItemStack): Int {
    return if (mainHandStack == stack)
        this.inventory.selectedSlot
    else
        PlayerInventory.OFF_HAND_SLOT
}

interface InfinityBucket {

    companion object {
        fun beforeLaunch() {
            addBasic(
                item = Items.BUCKET,
                Enchantments.INFINITY
            )

            addBasic(
                item = Items.WATER_BUCKET,
                Enchantments.INFINITY
            )
        }
    }

    fun handleEmptyInfinityBucket(stack: ItemStack, player: PlayerEntity, output: ItemStack): ItemStack {
        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
            (player as? ServerPlayerEntity)?.networkHandler?.sendPacket(ScreenHandlerSlotUpdateS2CPacket(-2, 0, player.slotMainOrOffHand(stack), stack))
            return stack
        }

        return ItemUsage.exchangeStack(stack, player, output)
    }

    fun handleWaterInfinityBucket(stack: ItemStack, player: PlayerEntity): ItemStack {
        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
            (player as? ServerPlayerEntity)?.networkHandler?.sendPacket(ScreenHandlerSlotUpdateS2CPacket(-2, 0, player.slotMainOrOffHand(stack), stack))
            return stack
        }

        return BucketItem.getEmptiedStack(stack, player)
    }
}