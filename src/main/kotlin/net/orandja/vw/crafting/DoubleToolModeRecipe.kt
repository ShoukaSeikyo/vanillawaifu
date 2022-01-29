package net.orandja.vw.crafting

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolItem
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.orandja.vw.maccessors.DoubleToolModeAccessor
import net.orandja.vw.mods.DoubleToolMode
import net.orandja.vw.utils.getPlayer
import net.orandja.vw.utils.toArray

class DoubleToolModeRecipe(val mcId: Identifier, val validModes: Array<DoubleToolMode>, val clazz: Class<*>) : CraftingRecipe {

//    companion object {
//        val VALID_MODES = arrayOf(DoubleToolMode.ALL, DoubleToolMode.SIMILAR, DoubleToolMode.VEIN)
//    }

    override fun getId(): Identifier {
        return mcId
    }

    override fun isIgnoredInRecipeBook(): Boolean {
        return true
    }

    override fun getOutput(): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun matches(craftingInventory: CraftingInventory, world: World?): Boolean {
        val playerInventory: PlayerInventory = craftingInventory.getPlayer()?.inventory ?: return false
        val offHand: Item = playerInventory.offHand[0].item ?: return false
        val inventory = craftingInventory.toArray(true)
        if(inventory.size != 1)
            return false

        return inventory.count { stack -> areToolSimilar(stack.item, offHand) } == 1
    }


    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }


    override fun craft(craftingInventory: CraftingInventory): ItemStack? {
        craftingInventory.toArray(true).forEach { println(it.item.name) }
        val output = craftingInventory.toArray(true).firstOrNull()?.copy() ?: return ItemStack.EMPTY
        val player = (craftingInventory.getPlayer() as? DoubleToolModeAccessor) ?: return ItemStack.EMPTY
        val toolMode = player.setNextToolMode(output, validModes)
        craftingInventory.getPlayer()?.sendMessage(LiteralText("Set tool mode to: ${toolMode.name}"), true)

        return ItemStack.EMPTY
    }

    override fun getSerializer(): RecipeSerializer<*>? {
        return RecipeSerializer.SHULKER_BOX
    }

    fun areToolSimilar(main: Item, off: Item): Boolean {
        return clazz.isInstance(main) && clazz.isInstance(off) && ((main as? ToolItem)?.material == (off as? PickaxeItem)?.material)
    }
}