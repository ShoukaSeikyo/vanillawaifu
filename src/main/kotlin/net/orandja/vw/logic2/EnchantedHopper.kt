package net.orandja.vw.logic2

import net.minecraft.block.BlockState
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.orandja.vw.mods.ProtectBlock
import net.orandja.vw.utils.canMergeItems
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.BooleanSupplier
import java.util.stream.IntStream
import kotlin.math.max

/**TODO:
 *  Understand all this old code.
 */

interface EnchantedHopper: BlockWithEnchantment, Inventory {

    fun getOutInventory(world: World, pos: BlockPos, state: BlockState): Inventory?
    fun getInvList(): DefaultedList<ItemStack>?
    fun isInvFull(inventory: Inventory, direction: Direction): Boolean
    fun isHopperFull(): Boolean
    fun markHopperDirty(world: World, pos: BlockPos, state: BlockState)
    fun setHopperCooldown(cooldown: Int)
    fun getOutputAvailableSlots(inventory: Inventory, side: Direction): IntStream
    fun isHopperEmpty(): Boolean

    val inventory: DefaultedList<ItemStack>

    var mending: Short
    var silkTouch: Short
    var efficiency: Short
    var transferCooldown: Int

    var hopperX: Double
    var hopperY: Double
    var hopperZ: Double

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "silk_touch" to silkTouch,
            "mending" to mending
        )
    }

    override fun hasEnchantments(): Boolean {
        return mending > 0 || efficiency > 0 || silkTouch > 0
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "efficiency" -> efficiency = level
            "silk_touch" -> silkTouch = level
            "mending" -> mending = level
        }
    }

    companion object {
        fun preventExtraction(world: World, hopper: Any, info: CallbackInfoReturnable<Boolean>) {
            if (hopper is EnchantedHopper && ProtectBlock.preventsExtract(world, hopper.hopperX, hopper.hopperY + 1.0, hopper.hopperZ)) {
                info.returnValue = false
            }
        }

        fun reduceCooldown(hopper: HopperBlockEntity) {
            if (hopper is EnchantedHopper) {
                val cooldown = 8 - if (hopper.hasEnchantments()) ((8 - 2) / 5.0 * hopper.efficiency).toInt() else 0
                hopper.setHopperCooldown(max(1, cooldown))
            }
        }

        fun filterIsInventoryEmpty(inventory: Inventory, facing: Direction): Boolean {
            if (inventory is EnchantedHopper) {
                val isFilter = ((inventory as? EnchantedHopper)?.silkTouch ?: 0) > 0
                return inventory.getOutputAvailableSlots(inventory, facing).allMatch { index ->
                    inventory.getStack(index).isEmpty || (isFilter && inventory.getStack(index).count == 1)
                }
            }

            return false
        }

        fun filterGetAvailableSlots(inventory: Inventory, facing: Direction): IntStream {
            val isFilter = ((inventory as? EnchantedHopper)?.silkTouch ?: 0) > 0
            val slots = if (inventory is SidedInventory)
                    IntStream.of(*inventory.getAvailableSlots(facing))
                else
                    IntStream.range(0, inventory.size())
            return slots.filter {
                !(inventory.getStack(it).isEmpty || (isFilter && inventory.getStack(it).count == 1))
            }
        }

        fun redstoneEnabled(world: World, pos: BlockPos, state: BlockState, blockEntity: HopperBlockEntity, booleanSupplier: BooleanSupplier): Comparable<*>? {
            val hopper: BlockEntity? = world.getBlockEntity(pos)
            if (hopper is EnchantedHopper) {
                if (hopper.silkTouch.toInt() == 1 && hopper.mending.toInt() == 0 && state.get(HopperBlock.ENABLED)) {
                    var bl = false
                    if (!hopper.isHopperEmpty()) {
                        bl = filterInsert(world, pos, state, blockEntity)
                    }
                    if (!hopper.isHopperFull()) {
                        bl = bl or booleanSupplier.asBoolean
                    }
                    if (bl) {
                        val cooldown = 8 - if (hopper.hasEnchantments()) ((8 - 2) / 5.0 * hopper.efficiency).toInt() else 0
                        hopper.setHopperCooldown(cooldown)
                        hopper.markHopperDirty(world, pos, state)
                    }
                    return false
                } else if (hopper.mending.toInt() == 1) {
                    if (!state.get(HopperBlock.ENABLED)) {
                        var bl = false
                        if (!hopper.isHopperEmpty()) {
                            bl = mendingRedstone(world, pos, state, blockEntity)
                        }
                        if (!hopper.isHopperFull()) {
                            bl = bl or booleanSupplier.asBoolean
                        }
                        if (bl) {
                            val cooldown = 8 - if (hopper.hasEnchantments()) ((8 - 2) / 5.0 * hopper.efficiency).toInt() else 0
                            hopper.setHopperCooldown(cooldown)
                            hopper.markHopperDirty(world, pos, state)
                        }
                    }
                    return false
                }
            }

            return state.get(HopperBlock.ENABLED)
        }

        fun filterInsert(world: World, pos: BlockPos, state: BlockState, inventory: Inventory): Boolean {
            val hopper: BlockEntity? = world.getBlockEntity(pos)
            if (hopper is EnchantedHopper) {
                val outputInventory = hopper.getOutInventory(world, pos, state)
                if (outputInventory != null) {
                    val direction = (state.get(HopperBlock.FACING) as Direction).opposite
                    if (!hopper.isInvFull(outputInventory, direction)) {
                        for (i in 0 until inventory.size()) {
                            val stackFrom = inventory.getStack(i)
                            if (stackFrom.isEmpty || stackFrom.count == 1) {
                                continue
                            }
                            val itemStack = stackFrom.copy()
                            val itemStack2 = HopperBlockEntity.transfer(
                                inventory,
                                outputInventory,
                                inventory.removeStack(i, 1),
                                direction
                            )
                            if (itemStack2.isEmpty) {
                                outputInventory.markDirty()
                                return true
                            }
                            inventory.setStack(i, itemStack)
                        }
                    }
                }
            }

            return false
        }

        fun mendingRedstone(world: World, pos: BlockPos, state: BlockState, inventory: Inventory): Boolean {
            val hopper: BlockEntity? = world.getBlockEntity(pos)
            if (hopper is EnchantedHopper) {
                val outputInventory = hopper.getOutInventory(world, pos, state)
                val power = world.getReceivedRedstonePower(pos) - 1
                if (power < 0 || outputInventory == null || power >= outputInventory.size()) {
                    return false
                }

                val outputStack = outputInventory.getStack(power)
                for (i in 0 until inventory.size()) {
                    val stackFromInv = inventory.getStack(i)
                    if (stackFromInv.isEmpty || (hopper.silkTouch.toInt() == 1 && stackFromInv.count == 1)) {
                        continue
                    }

                    if (outputStack.isEmpty) {
                        outputInventory.setStack(power, Inventories.splitStack(hopper.getInvList(), i, 1))
                        return true
                    }

                    if (canMergeItems(outputStack, stackFromInv, 1)) {
                        stackFromInv.decrement(1)
                        outputStack.increment(1)
                        inventory.setStack(i, stackFromInv)
                        outputInventory.setStack(power, outputStack)
                        return true
                    }
                }
            }

            return false
        }
    }
}