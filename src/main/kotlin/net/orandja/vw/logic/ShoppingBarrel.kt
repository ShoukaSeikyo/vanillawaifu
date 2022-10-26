package net.orandja.vw.logic

import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.village.TradeOffer
import net.minecraft.village.TradeOfferList
import net.minecraft.world.World
import net.orandja.mcutils.getOrCreate
import net.orandja.mcutils.gridStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

private fun NbtCompound.getStack(key: String): ItemStack {
    return ItemStack.fromNbt(this.getCompound(key))
}

class ShoppingBarrelRecipe(identifier: Identifier?) : SpecialCraftingRecipe(identifier), CustomRecipeInterceptor {

    private fun NbtCompound.compareOffer(other: NbtCompound): Boolean {
        return get("sell") == other.get("sell") &&
                get("buy") == other.get("buy") &&
                ((!this.contains("buyB") && !other.contains("buyB")) || (get("buyB") == other.get("buyB")))
    }

    private fun ItemStack.cleanCopy(): ItemStack {
        return ItemStack(item, count)
    }

    private fun ItemStack.toBarrelString(color: String = "white", colorCount: String = "white"): String {
        return """{"translate":"$translationKey","color":"$color"}, {"text":" x$count","color":"$colorCount"}"""

    }

    override fun matches(craftingInventory: CraftingInventory, world: World): Boolean {
        if(craftingInventory.width != 3 || craftingInventory.height != 3) {
            return false
        }

        val barrel = craftingInventory.gridStack(1, 0)
        val buyA = craftingInventory.gridStack(0, 1).cleanCopy()
        val sell = craftingInventory.gridStack(2, 1)

        return barrel.item == Items.BARREL && !barrel.isEmpty && !buyA.isEmpty && !sell.isEmpty
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
        return ItemStack(Items.BARREL)
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return SpecialRecipeSerializer.FIREWORK_ROCKET
    }

    override fun onTakeItem(input: CraftingInventory, player: PlayerEntity, slot: Int, amount: Int): ItemStack {
        return if(slot == 1) input.removeStack(slot, amount) else input.getStack(slot)
    }
}


interface ShoppingBarrel {

    companion object {

        var serializer: SpecialRecipeSerializer<ShoppingBarrelRecipe>? = null

        fun beforeLaunch() {
//            CustomRecipe.customShapedRecipes[Identifier("vanillawaifu", "shopping_barrel")] = ::ShoppingBarrelRecipe

            serializer = RecipeSerializer.register("vanillawaifu:shopping_barrel", SpecialRecipeSerializer { ShoppingBarrelRecipe(it) })
        }

        fun fromNBT(tag: NbtCompound): TradeOffer {
            if(tag.contains("buyB")) {
                return TradeOffer(
                    tag.getStack("buy"),
                    tag.getStack("buyB"),
                    tag.getStack("sell"),
                    99,
                    0,
                    0.0f
                )
            }
            return TradeOffer(
                tag.getStack("buy"),
                tag.getStack("sell"),
                99,
                0,
                0.0f
            )

        }

    }

    var offers: TradeOfferList?

    fun onShoppingPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack, info: CallbackInfo) {
        if (stack.nbt?.contains("Offers") == true) {
            val offers = TradeOfferList()
            (stack.nbt?.get("Offers") as NbtList).map { fromNBT(it as NbtCompound) }
                .forEach(offers::add)

            if(state.block is ShoppingBarrel)
                (world.getBlockEntity(pos) as ShoppingBarrel).offers = offers
        }
    }


    fun onShopUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult, info: CallbackInfoReturnable<ActionResult>) {
        val barrel = world.getBlockEntity(pos)
        if(barrel is ShoppingBarrel && barrel.offers != null) {
            if (world.isClient) {
                info.returnValue = ActionResult.SUCCESS
            }

            //net.minecraft.village.Merchant.sendOffers();

//            val blockEntity = world.getBlockEntity(pos)
//            if (blockEntity is BarrelBlockEntity) {
//                player.openHandledScreen(blockEntity as BarrelBlockEntity?)
//                player.incrementStat(Stats.OPEN_BARREL)
//                PiglinBrain.onGuardedBlockInteracted(player, true)
//            }
            info.returnValue = ActionResult.SUCCESS
        }
    }

    fun loadShop(tag: NbtCompound) {
        if (tag.contains("Offers", 10)) {
            offers = TradeOfferList(tag.getCompound("Offers"))
        }
    }

    fun saveShop(tag: NbtCompound) {
        if(offers != null) {
            if (!offers!!.isEmpty()) {
                tag.put("Offers", offers!!.toNbt())
            }
        }
    }

}