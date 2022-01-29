package net.orandja.vw.crafting

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class TransferEnchantOutputSlot(var input: Inventory, var result: Inventory, var context: ScreenHandlerContext) : Slot(result, 2, 129, 34) {

    companion object {
        var cost: Int = 30
        var destroyItem: Boolean = false
    }

    override fun canInsert(stack: ItemStack): Boolean {
        return false
    }

    override fun onTakeItem(player: PlayerEntity, stack: ItemStack) {
        if (getBookAndTool { _: ItemStack, _: ItemStack, bookSlot: Int, toolSlot: Int ->
                if (player.experienceLevel < cost) {
                    result.setStack(0, ItemStack.EMPTY)
                    return@getBookAndTool
                }

                player.addExperienceLevels(-cost)

                input.getStack(bookSlot).decrement(1)
                if(destroyItem) {
                    input.setStack(toolSlot, ItemStack.EMPTY)
                    return@getBookAndTool
                }

                val toolStack = input.getStack(toolSlot)
                toolStack.removeSubNbt("Enchantments")
                toolStack.removeSubNbt("StoredEnchantments")
                input.setStack(toolSlot, toolStack)
            }) {
            return
        }

        context.run { world: World, pos: BlockPos ->
            if (world is ServerWorld) {
                ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos), this.getExperience(world))
            }
            world.syncWorldEvent(1042, pos, 0)
        }
        input.setStack(0, ItemStack.EMPTY)
        input.setStack(1, ItemStack.EMPTY)
    }

    private fun getExperience(world: World): Int {
        val i = this.getExperience(input.getStack(0)) + this.getExperience(input.getStack(1))
        return if (i > 0) {
            val j = Math.ceil(i.toDouble() / 2.0).toInt()
            j + world.random.nextInt(j)
        } else {
            0
        }
    }

    private fun getExperience(stack: ItemStack): Int {
        var i = 0
        val map = EnchantmentHelper.get(stack)
        for (enchantment in map.keys) {
            if (!enchantment.isCursed) {
                i += enchantment.getMinPower(map[enchantment]!!)
            }
        }
        return i
    }

    fun getBookAndTool(consumer: (ItemStack, ItemStack, Int, Int) -> Unit): Boolean {
        val bookSlot = if (input.getStack(0).isOf(Items.BOOK)) 0 else if (input.getStack(1).isOf(Items.BOOK)) 1 else -1
        val toolSlot = if (input.getStack(0).isDamageable && input.getStack(0).hasEnchantments()) 0 else if (input.getStack(1).isDamageable && input.getStack(1).hasEnchantments()) 1 else -1
        if (bookSlot > -1 && toolSlot > -1) {
            consumer.invoke(input.getStack(bookSlot), input.getStack(toolSlot), bookSlot, toolSlot)
            return true
        }
        return false
    }

    fun getBookAndToolSlots(consumer: (Int, Int) -> Boolean): Boolean {
        val bookSlot = if (input.getStack(0).isOf(Items.BOOK)) 0 else if (input.getStack(1).isOf(Items.BOOK)) 1 else -1
        val toolSlot = if (input.getStack(0).isDamageable && input.getStack(0).hasEnchantments()) 0 else if (input.getStack(1).isDamageable && input.getStack(1).hasEnchantments()) 1 else -1
        if (bookSlot > -1 && toolSlot > -1) {
            consumer.invoke(bookSlot, toolSlot)
            return true
        }
        return false
    }
}