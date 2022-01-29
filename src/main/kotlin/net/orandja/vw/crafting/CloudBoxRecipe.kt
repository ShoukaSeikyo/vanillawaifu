package net.orandja.vw.crafting

import net.minecraft.block.Block
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtList
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.orandja.vw.utils.*

class CloudBoxRecipe(val mcId: Identifier) : CraftingRecipe {

    override fun getId(): Identifier? {
        return mcId
    }

    override fun isIgnoredInRecipeBook(): Boolean {
        return true
    }

    override fun getOutput(): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun matches(craftingInventory: CraftingInventory, world: World?): Boolean {
        val inventory = craftingInventory.toArray()
        val shulkerCount = inventory.count { stack ->
            Block.getBlockFromItem(stack.item) is ShulkerBoxBlock && stack.hasCustomName() && stack.name.asString().length > 2 &&
                !stack.nbt!!.contains("vw_channel") &&
                (!stack.nbt!!.contains("BlockEntityTag") || !stack.nbt!!.getCompound("BlockEntityTag").contains("Items") || stack.nbt!!.getCompound("BlockEntityTag").getList("Items", 10).size == 0)
        }
        val enderchestCount = inventory.count(Items.ENDER_CHEST::ofStack)
        return shulkerCount == 1 && enderchestCount == 1
    }


    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }


    override fun craft(craftingInventory: CraftingInventory): ItemStack? {
        val inventory = craftingInventory.toArray()
        val output = inventory.firstOrNull { stack -> Block.getBlockFromItem(stack.item) is ShulkerBoxBlock && stack.hasCustomName() }?.copy() ?: return ItemStack.EMPTY

        var channel = output.name.asString()
        var channelLiteral: String
        if (channel.startsWith(":", true)) {
            val player = craftingInventory.getPlayer() ?: return output
            channel = channel.substring(1);
            channelLiteral = "${channel} of ${player.entityName}"
            channel = "${player.uuidAsString}:${channel}"
        } else {
            channelLiteral = "$channel of public"
            channel = "public:${channel}"
        }

        output.computeTag { tag ->
            tag.putString("vw_channel", channel)
            tag.putString("vw_channel_literal", channelLiteral)
            tag.getOrCompute("Enchantments", { tag.get("Enchantments") as NbtList }, ::NbtList) {}
        }

        output.computeLore { lore ->
            lore.clear()
            lore + """{"text":"$channelLiteral", "color":"blue"}"""
        }

        output.setCustomName(Text.Serializer.fromJson("""{"text":"[Cloud Box] ","color":"green"}"""))

        return output
    }

    override fun getSerializer(): RecipeSerializer<*>? {
        return RecipeSerializer.SHULKER_BOX
    }
}