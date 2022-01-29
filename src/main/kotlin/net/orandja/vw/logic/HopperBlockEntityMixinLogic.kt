//package net.orandja.vw.mixinlogic
//
//import net.minecraft.block.BlockState
//import net.minecraft.block.HopperBlock
//import net.minecraft.block.entity.Hopper
//import net.minecraft.block.entity.HopperBlockEntity
//import net.minecraft.inventory.Inventories
//import net.minecraft.inventory.Inventory
//import net.minecraft.item.ItemStack
//import net.minecraft.util.collection.DefaultedList
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import net.minecraft.world.World
//import net.orandja.vw.maccessors.EnchantMoreAccessor
//import net.orandja.vw.mixin.HopperBlockEntityMixin
//import net.orandja.vw.mods.ProtectBlock.Companion.preventsExtract
//import net.orandja.vw.utils.canMergeItems
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
//import kotlin.math.max
//
//interface HopperBlockEntityMixinLogic: EnchantMoreAccessor {
//
//    companion object {
//        /**
//         * If a block is protected against extracting.
//         * ex.: WhitelistedChest
//         */
//        fun preventExtract(world: World, hopper: Hopper, info: CallbackInfoReturnable<Boolean>) {
//            if (preventsExtract(world, hopper.hopperX, hopper.hopperY + 1.0, hopper.hopperZ)) {
//                info.returnValue = false
//            }
//        }
//
//        fun enchantCooldown(entity: HopperBlockEntity) {
//            val hopper = entity as? HopperBlockEntityMixinLogic ?: return
//            val cooldownReduction = (if (hopper.isEnchanted()) ((hopper.transferCooldown - 2) / 5.0 * hopper.efficiency).toInt() else 0)
//            hopper.setHopperCooldown(max(1, hopper.transferCooldown - cooldownReduction))
//        }
//
//        fun enchantMending(world: World, pos: BlockPos, state: BlockState, inventory: Inventory): Boolean {
//            val hopper = world.getBlockEntity(pos) as? HopperBlockEntityMixinLogic ?: return false
//            val outInventory = HopperBlockEntityMixin.getOutputInventory(world, pos, state)
//            val redstonePower = world.getReceivedRedstonePower(pos) - 1
//            val outStack: ItemStack = if(redstonePower < 0 || outInventory == null || redstonePower >= outInventory.size()) {
//                outInventory.getStack(redstonePower)
//            } else return false
//
//            for (i in 0..inventory.size()) {
//                val inStack = inventory.getStack(i)
//
//                if (inStack.isEmpty || hopper.silkTouch.toInt() == 1 && inStack.count == 1) {
//                    continue
//                }
//
//                if (outStack.isEmpty) {
//                    outInventory.setStack(redstonePower, Inventories.splitStack(hopper.getHopperInventory(), i, 1))
//                    return true
//                }
//
//                if (canMergeItems(inStack, outStack)) {
//                    inStack.decrement(1)
//                    outStack.increment(1)
//                    inventory.setStack(i, inStack)
//                    outInventory.setStack(redstonePower, outStack)
//                    return true
//                }
//            }
//
//            return false
//        }
//
//        fun enchantSilkTouch(world: World, pos: BlockPos, state: BlockState, inventory: Inventory): Boolean {
//            val inventory2 = HopperBlockEntityMixin.getOutputInventory(world, pos, state)
//            return if (inventory2 == null) {
//                false
//            } else {
//                val direction = (state.get(HopperBlock.FACING) as Direction).opposite
//                if (HopperBlockEntityMixin.isInventoryFull(inventory2, direction)) {
//                    false
//                } else {
//                    for (i in 0 until inventory.size()) {
//                        val stackFrom = inventory.getStack(i)
//                        if (stackFrom.isEmpty || stackFrom.count == 1) {
//                            continue
//                        }
//                        val itemStack = stackFrom.copy()
//                        val itemStack2 = HopperBlockEntity.transfer(inventory, inventory2, inventory.removeStack(i, 1), direction)
//                        if (itemStack2.isEmpty) {
//                            inventory2.markDirty()
//                            return true
//                        }
//                        inventory.setStack(i, itemStack)
//                    }
//                    false
//                }
//            }
//        }
//    }
//
//    var transferCooldown: Int
//
//    var mending: Short
//    var silkTouch: Short
//    var efficiency: Short
//
//    fun setHopperCooldown(cooldown: Int)
//    fun getHopperCooldown(): Int
//
//    fun getHopperInventory(): DefaultedList<ItemStack>
//
//    override fun getEnchantments(): Map<String, Short> {
//        return java.util.Map.of(
//            "efficiency", efficiency,
//            "mending", mending,
//            "silk_touch", silkTouch
//        )
//    }
//
//    override fun isEnchanted(): Boolean {
//        return mending > 0 || efficiency > 0 || silkTouch > 0
//    }
//
//    override fun applyEnchantments(name: String, level: Short) {
//        when (name) {
//            "efficiency" -> {
//                efficiency = level
//                return
//            }
//            "mending" -> {
//                mending = level
//                return
//            }
//            "silk_touch" -> {
//                silkTouch = level
//                return
//            }
//        }
//    }
//}