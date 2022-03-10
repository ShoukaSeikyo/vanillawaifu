package net.orandja.vw.logic

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.recipe.*
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.orandja.mcutils.getOrCreate
import net.orandja.mcutils.getTagOrCreate
import net.orandja.mcutils.gridStack

class ShoppingBarrelRecipe(identifier: Identifier?) : SpecialCraftingRecipe(identifier), CustomRecipeInterceptor {

    private fun NbtCompound.compareOffer(other: NbtCompound): Boolean {
        return get("sell") == other.get("sell") &&
                get("buy") == other.get("buy") &&
                ((!this.contains("buyB") && !other.contains("buyB")) || (get("buyB") == other.get("buyB")))
    }

    private fun ItemStack.cleanCopy(): ItemStack {
        return ItemStack(item, count)
    }

    private fun NbtCompound.getStack(key: String): ItemStack {
        return ItemStack.fromNbt(this.getCompound(key))
    }

    private fun ItemStack.toBarrelString(color: String = "white", colorCount: String = "white"): String {
//        return """{"text":"${Registry.ITEM.getKey(item).get().value.path}","color":"$color"}, {"text":" x$count","color":"$colorCount"}"""
        return """{"translate":"$translationKey","color":"$color"}, {"text":" x$count","color":"$colorCount"}"""

    }

    override fun matches(craftingInventory: CraftingInventory, world: World): Boolean {
        if(craftingInventory.width != 3 || craftingInventory.height != 3) {
            return false
        }

        val barrel = craftingInventory.gridStack(1, 0)
        val buyA = craftingInventory.gridStack(0, 1).cleanCopy()
        val buyB = craftingInventory.gridStack(1, 1).cleanCopy()
        val sell = craftingInventory.gridStack(2, 1)

        return !barrel.isEmpty && !buyA.isEmpty && !sell.isEmpty
    }

    override fun craft(craftingInventory: CraftingInventory): ItemStack {
        val output = craftingInventory.gridStack(1, 0).copy()

        val buyA = craftingInventory.gridStack(0, 1)
        val buyB = craftingInventory.gridStack(1, 1)
        val sell = craftingInventory.gridStack(2, 1)

        val offer = NbtCompound().apply {
            this.put("sell", sell.writeNbt(NbtCompound()))
            this.put("buy", buyA.writeNbt(NbtCompound()))
            if(!buyB.isEmpty)
                this.put("buyB", buyB.writeNbt(NbtCompound()))
        }

        val offers = output.orCreateNbt.getOrCreate("Offers", { output.orCreateNbt.getList(it, 10) }) { NbtList() }
        val existing = offers.firstOrNull {
            (it as NbtCompound).compareOffer(offer)
        }

        if(existing != null) {
            offers.remove(existing)
        } else {
            offers.add(offer)
        }

        val display = output.orCreateNbt.getOrCreate("display", output.orCreateNbt::get, ::NbtCompound) as NbtCompound
        display.put("Lore", NbtList().apply {
            add(NbtString.of("""{"text":"Shopping Barrel: ","color":"green"}"""))
            offers.forEach {
                it as NbtCompound
                add(NbtString.of("""[
                    ${it.getStack("buy").toBarrelString()},
                    ${ if(it.contains("buyB")) """{"text": " + "},${it.getStack("buyB").toBarrelString()},""" else "" }
                    {"text":" -> ", "color": "green"},
                    ${it.getStack("sell").toBarrelString(color = "dark_purple")}
                    ]""".trimMargin()))
            }
        })

        return output
    }

    override fun getRemainder(inventory: CraftingInventory): DefaultedList<ItemStack>? {
        val defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY)
        defaultedList.forEachIndexed { index, stack ->
            defaultedList[index] = if(index == 1) ItemStack(stack.item.recipeRemainder) else stack.copy().apply {
                this.count++
            }
        }
        return defaultedList
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getOutput(): ItemStack {
        return ItemStack(Items.FIREWORK_ROCKET)
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RecipeSerializer.FIREWORK_ROCKET
    }

    override fun onTakeItem(input: CraftingInventory, player: PlayerEntity, slot: Int, amount: Int): ItemStack {
        return if(slot == 1) input.removeStack(slot, amount) else input.getStack(slot)
    }
}


interface ShoppingBarrel {

    companion object {
        fun beforeLaunch() {
//            CustomRecipe.customShapedRecipes[Identifier("vanillawaifu", "shopping_barrel")] = ::ShoppingBarrelRecipe

            RecipeSerializer.register("vanillawaifu:shopping_barrel", SpecialRecipeSerializer { ShoppingBarrelRecipe(it) })
        }

    }

}