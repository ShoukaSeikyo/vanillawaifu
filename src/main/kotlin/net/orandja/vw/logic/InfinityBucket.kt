package net.orandja.vw.logic

import net.minecraft.block.*
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.block.dispenser.ItemDispenserBehavior
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.DispenserBlockEntity
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.*
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPointer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.orandja.vw.logic.EnchantMore.Companion.addBasic
import java.util.function.Predicate

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

            //Cannot mixin into an Interface. Hard change
            CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR[Items.WATER_BUCKET] =
                CauldronBehavior { state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack ->
                    fillCauldron(world, pos, player, hand, stack, Blocks.WATER_CAULDRON.defaultState.with(LeveledCauldronBlock.LEVEL, 3) as BlockState, SoundEvents.ITEM_BUCKET_EMPTY)
                }
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR[Items.WATER_BUCKET] =
                CauldronBehavior { state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack ->
                    fillCauldron(world, pos, player, hand, stack, Blocks.WATER_CAULDRON.defaultState.with(LeveledCauldronBlock.LEVEL, 3) as BlockState, SoundEvents.ITEM_BUCKET_EMPTY)
                }
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR[Items.BUCKET] =
                CauldronBehavior { state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack ->
                    emptyCauldron(state, world, pos, player, hand, stack, ItemStack(Items.WATER_BUCKET), { it.get(LeveledCauldronBlock.LEVEL) == 3 }, SoundEvents.ITEM_BUCKET_FILL)
                }

            DispenserBlock.registerBehavior(Items.WATER_BUCKET, object : ItemDispenserBehavior() {
                private val fallbackBehavior = ItemDispenserBehavior()
                override fun dispenseSilently(pointer: BlockPointer, stack: ItemStack): ItemStack {
                    val fluidModificationItem = stack.item as Any as FluidModificationItem
                    val blockPos = pointer.pos.offset(pointer.blockState.get(DispenserBlock.FACING))
                    val world = pointer.world
                    if (fluidModificationItem.placeFluid(null, world, blockPos, null)) {
                        fluidModificationItem.onEmptied(null, world, stack, blockPos)
                        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
                            return stack
                        }
                        return ItemStack(Items.BUCKET)
                    }
                    return fallbackBehavior.dispense(pointer, stack)
                }
            })

            DispenserBlock.registerBehavior(Items.BUCKET, object : ItemDispenserBehavior() {
                private val fallbackBehavior = ItemDispenserBehavior()
                override fun dispenseSilently(pointer: BlockPointer, stack: ItemStack): ItemStack {
                    val itemStack: ItemStack
                    var blockPos: BlockPos?
                    val worldAccess = pointer.world
                    val blockState = worldAccess.getBlockState(
                        pointer.pos.offset(pointer.blockState.get(DispenserBlock.FACING)).also {
                            blockPos = it
                        })
                    val block = blockState.block
                    if (block is FluidDrainable) {
                        itemStack = (block as Any as FluidDrainable).tryDrainFluid(worldAccess, blockPos, blockState)
                        if (itemStack.isEmpty) {
                            return super.dispenseSilently(pointer, stack)
                        }
                    } else {
                        return super.dispenseSilently(pointer, stack)
                    }
                    worldAccess.emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos)
                    if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
                        return stack
                    }
                    val item = itemStack.item
                    stack.decrement(1)
                    if (stack.isEmpty) {
                        return ItemStack(item)
                    }
                    if ((pointer.getBlockEntity<BlockEntity>() as DispenserBlockEntity).addToFirstFreeSlot(ItemStack(item)) < 0) {
                        fallbackBehavior.dispense(pointer, ItemStack(item))
                    }
                    return stack
                }
            })

        }

        fun emptyCauldron(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack, output: ItemStack, predicate: Predicate<BlockState>, soundEvent: SoundEvent): ActionResult {
            if (!predicate.test(state)) {
                return ActionResult.PASS
            }
            if (!world.isClient) {
                val item = stack.item
                player.setStackInHand(hand, handleEmptyInfinityBucket(stack, player, output))
                player.incrementStat(Stats.USE_CAULDRON)
                player.incrementStat(Stats.USED.getOrCreateStat(item))
                world.setBlockState(pos, Blocks.CAULDRON.defaultState)
                world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos)
            }
            return ActionResult.success(world.isClient)
        }

        fun fillCauldron(world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, stack: ItemStack, state: BlockState, soundEvent: SoundEvent): ActionResult {
            if (!world.isClient) {
                val item = stack.item
                player.setStackInHand(hand, handleWaterInfinityBucket(stack, player, ItemStack(Items.BUCKET)))
                player.incrementStat(Stats.FILL_CAULDRON)
                player.incrementStat(Stats.USED.getOrCreateStat(item))
                world.setBlockState(pos, state)
                world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f)
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos)
            }
            return ActionResult.success(world.isClient)
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

        fun handleWaterInfinityBucket(stack: ItemStack, player: PlayerEntity, output: ItemStack): ItemStack {
            if (EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0) {
                (player as? ServerPlayerEntity)?.networkHandler?.sendPacket(ScreenHandlerSlotUpdateS2CPacket(-2, 0, player.slotMainOrOffHand(stack), stack))
                return stack
            }

            return ItemUsage.exchangeStack(stack, player, output)
        }
    }
}