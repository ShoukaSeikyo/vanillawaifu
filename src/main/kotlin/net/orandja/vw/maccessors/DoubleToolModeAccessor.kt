package net.orandja.vw.maccessors

import net.minecraft.item.ItemStack
import net.orandja.vw.mods.DoubleToolMode

interface DoubleToolModeAccessor {
    fun getToolMode(stack: ItemStack): DoubleToolMode

    fun setToolMode(stack: ItemStack, mode: DoubleToolMode): DoubleToolMode
    fun setNextToolMode(stack: ItemStack, validModes: Array<DoubleToolMode>): DoubleToolMode
}