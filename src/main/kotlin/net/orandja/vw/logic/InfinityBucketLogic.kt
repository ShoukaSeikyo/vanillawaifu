package net.orandja.vw.logic

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

fun PlayerEntity.slotMainOrOffHand(stack: ItemStack): Int {
    if (mainHandStack == stack) {
        return this.inventory.selectedSlot
    } else {
        return PlayerInventory.OFF_HAND_SLOT
    }
}

interface InfinityBucketLogic {

    fun onStackChange(stack: ItemStack, player: PlayerEntity, output: ItemStack): ItemStack {
        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
            (player as? ServerPlayerEntity)?.networkHandler?.sendPacket(ScreenHandlerSlotUpdateS2CPacket(-2, 0, player.slotMainOrOffHand(stack), stack))
            return stack
        }

        return ItemUsage.exchangeStack(stack, player, output)
    }

}