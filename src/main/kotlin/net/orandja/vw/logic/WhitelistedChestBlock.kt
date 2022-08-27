package net.orandja.vw.logic

import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.enums.ChestType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.mcutils._addAll
import net.orandja.mcutils.getOrCreate
import net.orandja.mcutils.getPlayer
import net.orandja.mcutils.hasListeners
import net.orandja.vw.VW
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*

private fun ArrayList<String>.addNbt(nbt: NbtCompound) {
    if (nbt.contains("whitelist"))
        nbt.getList("whitelist", 8).map(NbtElement::asString).forEach(::add)
}

private fun ArrayList<String>.toNBT(nbt: NbtCompound) {
    if (size > 0)
        nbt.put("whitelist", NbtList()._addAll(map(NbtString::of)))
}

private fun String.asUserName(): String {
    return VW.serverReference.get().userCache.getByUuid(UUID.fromString(this)).get().name
}

private fun ItemStack.hasWhiteList(): Boolean {
    return nbt?.contains("whitelist") ?: false
}

private fun ServerPlayerEntity.sendBlockUpdate(world: World, vararg pos: BlockPos) {
    pos.forEach { networkHandler.sendPacket(BlockUpdateS2CPacket(world, it)) }
}

private fun BlockState.getAdjacentPos(pos: BlockPos): BlockPos? {
    val chestType = get(ChestBlock.CHEST_TYPE)
    if (chestType == ChestType.SINGLE) {
        return null
    }

    return pos.offset(get(ChestBlock.FACING).let {
        when (chestType) {
            ChestType.RIGHT -> it.rotateYCounterclockwise()
            ChestType.LEFT -> it.rotateYClockwise()
            else -> it
        }
    })
}

interface WhitelistedChestBlock {

    companion object {
        fun getChest(world: World, pos: BlockPos): WhitelistedChestBlock? {
            return if (!world.isClient) world.getBlockEntity(pos) as? WhitelistedChestBlock else null
        }

        fun beforeLaunch() {
            CustomRecipe.customShapelessRecipes[Identifier("vanillawaifu", "whitelisted_chest")] =
                ::WhitelistedChestRecipe
            ProtectBlock.DESTROY.add { world, pos, entity -> getChest(world, pos)?.canDestroy(entity) ?: true }
            ProtectBlock.EXPLOSION_PROTECTION.add { world, pos -> getChest(world, pos)?.hasNoWhitelist() == false }
            ProtectBlock.EXTRACTION_PREVENTION.add { world, pos -> getChest(world, pos)?.hasWhitelist() == true }
        }
    }

    var whitelist: ArrayList<String>

    fun loadWhitelist(nbt: NbtCompound) {
        whitelist.addNbt(nbt)
    }
    fun saveWhitelist(nbt: NbtCompound) {
        whitelist.toNBT(nbt)
    }
    fun hasWhitelist(): Boolean {
        return whitelist.size > 0
    }
    fun hasNoWhitelist(): Boolean {
        return whitelist.size == 0
    }

    fun canDestroy(player: Entity): Boolean {
        return !hasWhitelist() || isWhitelisted(player as? PlayerEntity ?: return false)
    }

    private fun isWhitelisted(player: PlayerEntity): Boolean =
        !hasWhitelist() || player.isCreative || whitelist.contains(player.uuidAsString)

    private fun compareStack(stack: ItemStack): Boolean {
        return (stack.nbt?.getList("whitelist", 8)?.map(NbtElement::asString)
            ?: return false).count { whitelist.contains(it) } == whitelist.size
    }

    private fun handlePlayer(player: PlayerEntity, info: CallbackInfoReturnable<ActionResult>) {
        if (!isWhitelisted(player)) {
            player.sendMessage(
                Text.Serializer.fromJson(
                    """[{"text":"Whitelisted for: ","color":"red"},{"text":"${
                        whitelist.joinToString(
                            ",",
                            transform = String::asUserName
                        )
                    }","color":"green"}]"""
                ), true
            )
            info.returnValue = ActionResult.SUCCESS
        }
    }

    private fun handleStack(stack: ItemStack) {
        stack.nbt?.getList("whitelist", 8)?.map(NbtElement::asString)?.forEach(whitelist::add)
    }

    fun onBlockUse(world: World, pos: BlockPos, player: PlayerEntity, info: CallbackInfoReturnable<ActionResult>) {
        getChest(world, pos)?.handlePlayer(player, info)
    }

    fun onBlockPlaced(world: World, pos: BlockPos, stack: ItemStack, info: CallbackInfo) {
        getChest(world, pos)?.handleStack(stack)
    }

    fun onPlacementState(ctx: ItemPlacementContext, info: CallbackInfoReturnable<BlockState>) {
        if (ctx.world.isClient)
            return

        val adjacentPos = info.returnValue.getAdjacentPos(ctx.blockPos) ?: return
        val adjacentChest = getChest(ctx.world, adjacentPos)

        if ((ctx.stack.hasWhiteList() && adjacentChest?.compareStack(ctx.stack) == true) || adjacentChest?.hasWhitelist() == false) {
            return
        }

        (ctx.player as? ServerPlayerEntity)?.sendBlockUpdate(ctx.world, ctx.blockPos, adjacentPos)
        info.returnValue = info.returnValue.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE)
    }
}

class WhitelistedChestRecipe(id: Identifier, group: String, output: ItemStack, ingredients: DefaultedList<Ingredient>) :
    ShapelessRecipe(id, group, output, ingredients) {

    override fun matches(inventory: CraftingInventory, world: World): Boolean {
        return inventory.hasListeners() && inventory.getPlayer() != null && super.matches(inventory, world)
    }

    override fun craft(inventory: CraftingInventory): ItemStack {
        val player = inventory.getPlayer() ?: return ItemStack.EMPTY
        if (inventory.hasListeners()) {
            val chestOutput = output.copy()

            chestOutput.orCreateNbt.apply {
                val whitelist =
                    this.getOrCreate("whitelist", { this.getList(it, 8) }, ::NbtList) ?: return ItemStack.EMPTY
                if (whitelist.map(NbtElement::asString).find(player.uuidAsString::equals) == null) {
                    whitelist.add(NbtString.of(player.uuidAsString))

                    val display = this.getOrCreate("display", this::get, ::NbtCompound) as NbtCompound
                    val list = display.getOrCreate("Lore", display::get) {
                        val lore = NbtList()
                        lore.add(NbtString.of("""{"text":"Whitelisted Chest allowed for: ","color":"green"}"""))
                        lore
                    } as NbtList

                    list.add(NbtString.of("""{"text":"— ${player.name.string}","color":"green"}"""))
                }
            }

            return chestOutput
        }
        return ItemStack.EMPTY
    }
}