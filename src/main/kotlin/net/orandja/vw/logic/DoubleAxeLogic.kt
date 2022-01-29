package net.orandja.vw.logic

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.mods.DoubleToolMode
import net.orandja.vw.mods.DoubleTools.Companion.areBothSuitable
import net.orandja.vw.mods.DoubleTools.Companion.areBothToolsSuitable
import net.orandja.vw.mods.DoubleTools.Companion.getBothTools
import net.orandja.vw.mods.anyToolBreaking
import net.orandja.vw.mods.isWood

interface DoubleAxeLogic {
//    fun use(value: Boolean, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, player: LivingEntity): Boolean {
//        if (!world.isClient && player is PlayerEntity && areBothSuitable(player, state, AxeItem::class.java)) {
//            DoubleToolMode.AXE.task(world, pos, player, state, player.getBothTools()) {
//                player.mainHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
//                player.offHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND) }
//
//                player.incrementStat(Stats.MINED.getOrCreateStat(state.block))
//                player.addExhaustion(0.005f)
//                player.tryBreakBlock(it)
//            }
//        }
//
//        return value
//    }

    fun useDoubleAxe(block: Block, blockEntity: BlockEntity?, world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, stack: ItemStack): Boolean {
        if (!world.isClient && state.isWood() && player.areBothToolsSuitable(state, AxeItem::class.java) && !player.anyToolBreaking()) {
            player.incrementStat(Stats.MINED.getOrCreateStat(block))
            player.addExhaustion(0.005f)

            var count = 0
            DoubleToolMode.AXE.task(world, pos, player, state, player.getBothTools()) {
                player.mainHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
                player.offHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND) }

                player.incrementStat(Stats.MINED.getOrCreateStat(state.block))
                player.addExhaustion(0.005f)
                world.setBlockState(it, Blocks.AIR.defaultState, 2)
                count++
            }

            if (world is ServerWorld) {
                Block.getDroppedStacks(state, world, pos, blockEntity, player, stack).forEach {
                    it.increment(count)
                    Block.dropStack(world, pos, it)
                }
                state.onStacksDropped(world, pos, stack)
            }
            return false
        }

        // Executes Default afterBreak
        return true
    }
}