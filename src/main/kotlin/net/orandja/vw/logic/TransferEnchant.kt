//package net.orandja.vw.logic
//
//import net.minecraft.enchantment.EnchantmentHelper
//import net.minecraft.entity.ExperienceOrbEntity
//import net.minecraft.entity.player.PlayerEntity
//import net.minecraft.entity.player.PlayerInventory
//import net.minecraft.inventory.Inventory
//import net.minecraft.item.ItemStack
//import net.minecraft.item.Items
//import net.minecraft.nbt.NbtCompound
//import net.minecraft.nbt.NbtList
//import net.minecraft.screen.ScreenHandlerContext
//import net.minecraft.screen.slot.Slot
//import net.minecraft.server.world.ServerWorld
//import net.minecraft.util.collection.DefaultedList
//import net.minecraft.util.math.Vec3d
//import net.minecraft.world.World
//import net.orandja.mcutils.hasAnyEnchantments
//import net.orandja.vw.logic.TransferEnchantOutputSlot.Companion.cost
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
//import kotlin.math.ceil
//
//
//class TransferEnchantSlot(inventory: Inventory, index: Int, x: Int, y: Int): Slot(inventory, index, x, y) {
//    override fun canInsert(stack: ItemStack): Boolean {
//        return stack.isDamageable || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasAnyEnchantments() || stack.isOf(Items.BOOK)
//    }
//}
//
//class TransferEnchantOutputSlot(var input: Inventory, var result: Inventory, var context: ScreenHandlerContext) : Slot(result, 2, 129, 34) {
//
//    companion object {
//        var cost: Int = 8
//        var destroyItem: Boolean = false
//    }
//
//    override fun canInsert(stack: ItemStack): Boolean {
//        return false
//    }
//
//    override fun onTakeItem(player: PlayerEntity, stack: ItemStack) {
//        if (getBookAndTool { _: ItemStack, _: ItemStack, bookSlot: Int, toolSlot: Int ->
//                if (player.experienceLevel < cost) {
//                    result.setStack(0, ItemStack.EMPTY)
//                    return@getBookAndTool
//                }
//
//                player.addExperienceLevels(-cost)
//
//                input.getStack(bookSlot).decrement(1)
//                if(destroyItem) {
//                    input.setStack(toolSlot, ItemStack.EMPTY)
//                    return@getBookAndTool
//                }
//
//                var toolStack = input.getStack(toolSlot)
//                stack.nbt!!.getList("StoredEnchantments", 10).forEach {
//                    val enchantmentKey = if(toolStack.item == Items.ENCHANTED_BOOK) "StoredEnchantments" else "Enchantments"
//                    val enchantmentList = toolStack.nbt!!.getList(enchantmentKey, 10)
//
//                    it as NbtCompound
//                    val enchant = it.getString("id")
//                    val level = it.getShort("lvl")
//
//                    for (i in 0 until enchantmentList.size) {
//                        val toolTag = enchantmentList[i] as NbtCompound
//                        val toolEnchant = toolTag.getString("id")
//                        val toolLevel = toolTag.getShort("lvl")
//                        if(toolEnchant == enchant && toolLevel == level) {
//                            enchantmentList.removeAt(i)
//                            break;
//                        }
//                    }
//
//                    if(enchantmentList.size == 0) {
//                        if(toolStack.item == Items.ENCHANTED_BOOK) {
//                            toolStack = ItemStack(Items.BOOK, 1)
//                        } else {
//                            toolStack.removeSubNbt(enchantmentKey)
//                        }
//                    }
//                }
//                input.setStack(toolSlot, toolStack)
//            }) {
//            return
//        }
//
//        context.run { world, pos ->
//            if (world is ServerWorld) {
//                ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos), this.getExperience(world))
//            }
//            world.syncWorldEvent(1042, pos, 0)
//        }
//        input.setStack(0, ItemStack.EMPTY)
//        input.setStack(1, ItemStack.EMPTY)
//    }
//
//    private fun getExperience(world: World): Int {
//        val i = this.getExperience(input.getStack(0)) + this.getExperience(input.getStack(1))
//        return if (i > 0) {
//            val j = ceil(i.toDouble() / 2.0).toInt()
//            j + world.random.nextInt(j)
//        } else
//            0
//    }
//
//    private fun getExperience(stack: ItemStack): Int {
//        var i = 0
//        val map = EnchantmentHelper.get(stack)
//        for (enchantment in map.keys) {
//            if (!enchantment.isCursed) {
//                i += enchantment.getMinPower(map[enchantment]!!)
//            }
//        }
//        return i
//    }
//
//    fun getBookAndTool(consumer: (ItemStack, ItemStack, Int, Int) -> Unit): Boolean {
//        val bookSlot = if (input.getStack(0).isOf(Items.BOOK)) 0 else if (input.getStack(1).isOf(Items.BOOK)) 1 else -1
//        val toolSlot = if (input.getStack(0).hasAnyEnchantments()) 0 else if (input.getStack(1).hasAnyEnchantments()) 1 else -1
//        if (bookSlot > -1 && toolSlot > -1) {
//            consumer(input.getStack(bookSlot), input.getStack(toolSlot), bookSlot, toolSlot)
//            return true
//        }
//        return false
//    }
//
//    fun getBookAndToolSlots(consumer: (Int, Int) -> Unit): Boolean {
//        val bookSlot = if (input.getStack(0).isOf(Items.BOOK)) 0 else if (input.getStack(1).isOf(Items.BOOK)) 1 else -1
//        val toolSlot = if (input.getStack(0).hasAnyEnchantments()) 0 else if (input.getStack(1).hasAnyEnchantments()) 1 else -1
//        if (bookSlot > -1 && toolSlot > -1) {
//            consumer(bookSlot, toolSlot)
//            return true
//        }
//        return false
//    }
//}
//
//interface TransferEnchant {
//
//    val input: Inventory
//    val result: Inventory
//    var outputSlot: TransferEnchantOutputSlot
//    var playerInventory: PlayerInventory
//    val context: ScreenHandlerContext
//
//    fun onInit(syncId: Int, playerInventory: PlayerInventory, context: ScreenHandlerContext) {
//        this.playerInventory = playerInventory
//
//        replaceSlot(TransferEnchantSlot(input, 0, 49, 19), 0)
//        replaceSlot(TransferEnchantSlot(input, 1, 49, 40), 1)
//
//        replaceSlot(TransferEnchantOutputSlot(input, result, context).also { outputSlot = it }, 2)
//    }
//
//    fun replaceSlot(slot: Slot, id: Int)
//
//    fun updateContents()
//
//    fun updateTransferResult(info: CallbackInfo) {
//        outputSlot.getBookAndToolSlots { bookSlot, toolSlot ->
//            if (playerInventory.player.experienceLevel < cost) {
//                result.setStack(0, ItemStack.EMPTY)
//            } else {
//                val outputStack = ItemStack(Items.ENCHANTED_BOOK, 1)
//                val toolStack = input.getStack(toolSlot)
//                val enchantmentKey = if(toolStack.item == Items.ENCHANTED_BOOK) "StoredEnchantments" else "Enchantments"
//                val key = (0 until (toolStack.nbt!!.getList(enchantmentKey, 10).size)).random()
//                outputStack.orCreateNbt.put("StoredEnchantments", NbtList().apply {
//                    this.add(toolStack.nbt!!.getList(enchantmentKey, 10)[key].copy())
//                })
//                result.setStack(0, outputStack)
//                this.updateContents()
//            }
//            info.cancel()
//        }
//    }
//}