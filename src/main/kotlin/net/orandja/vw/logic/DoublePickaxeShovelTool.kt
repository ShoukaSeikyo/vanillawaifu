package net.orandja.vw.logic

import net.minecraft.block.BlockState
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ShovelItem
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.mods.DoubleTools
import net.orandja.vw.mods.DoubleTools.Companion.getBothTools
import net.orandja.vw.mods.DoubleTools.Companion.getToolMode
import net.orandja.vw.mods.DoubleTools.Companion.tryBreakBlock

interface DoublePickaxeShovelTool {

    fun use(value: Boolean, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, player: LivingEntity, clazz: Class<*>): Boolean {
        if (!world.isClient && player is PlayerEntity && DoubleTools.areBothSuitable(player, state, clazz)) {
            player.getToolMode().task(world, pos, player, state, player.getBothTools()) {
                player.mainHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
                player.offHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND) }

                player.incrementStat(Stats.MINED.getOrCreateStat(state.block))
                player.addExhaustion(0.005f)
                player.tryBreakBlock(it)
            }
        }

        return value
    }

}