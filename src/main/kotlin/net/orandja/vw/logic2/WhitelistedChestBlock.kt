package net.orandja.vw.logic2

import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.enums.ChestType
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.VW
import net.orandja.vw.crafting.ModifierShapedRecipe
import net.orandja.vw.mods.ProtectBlock
import net.orandja.vw.utils.*
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
        when(chestType) {
            ChestType.RIGHT -> it.rotateYCounterclockwise()
            ChestType.LEFT -> it.rotateYClockwise()
            else -> it
        }
    })
}

interface WhitelistedChestBlock {

    companion object {
        fun getChest(world: World, pos: BlockPos): WhitelistedChestBlock? = world.getBlockEntity(pos) as? WhitelistedChestBlock

        fun launch() {
            ProtectBlock.DESTROY.add { world, pos, entity -> getChest(world, pos)?.canDestroy(entity) ?: true }
            ProtectBlock.EXPLOSIONS.add { world, pos -> getChest(world, pos)?.hasWhitelist() == false }
            ProtectBlock.EXTRACTORS.add { world, pos -> getChest(world, pos)?.hasWhitelist() == true }

            VWRecipes.addRecipe(ModifierShapedRecipe(
                Identifier("vanillawaifu", "whitelisted_chest"), "", 1, 1,
                VWRecipes.getIngredients(arrayOf("C"), mapOf("C" to VWRecipes.ingredient("chest")), 1, 1),
                ItemStack(Items.CHEST)
            ) { inventory, player, normalOutput ->
                val output = inventory.toArray().first(Items.CHEST::ofStack).copy()
                output.count = normalOutput.count

                output.computeTag { tag ->
                    tag.getSLOrCompute("whitelist") { whitelist ->
                        if (whitelist.map(NbtElement::asString).find(player.uuidAsString::equals) == null) {
                            whitelist + player.uuidAsString

                            tag.getTagOrCompute("display") { display ->
                                display.getSLOrCompute("Lore", ::initWhitelistLore) { lore ->
                                    lore + """{"text":"— ${player.name.asString()}","color":"green"}"""
                                }
                            }
                        }
                    }
                }

                output
            })
        }

        private fun initWhitelistLore(): NbtList {
            val list = NbtList()
            list + """{"text":"Whitelisted Chest allowed for: ","color":"green"}"""
            return list
        }
    }

    var whitelist: ArrayList<String>

    fun loadWhitelist(nbt: NbtCompound) = whitelist::addNbt
    fun saveWhitelist(nbt: NbtCompound) = whitelist::toNBT
    fun hasWhitelist(): Boolean = whitelist.size > 0
    fun canDestroy(player: Entity): Boolean {
        return !hasWhitelist() || isWhitelisted(player as? PlayerEntity ?: return false)
    }

    private fun isWhitelisted(player: PlayerEntity): Boolean = !hasWhitelist() || player.isCreative || whitelist.contains(player.uuidAsString)
    private fun compareStack(stack: ItemStack): Boolean {
        return (stack.nbt?.getList("whitelist", 8)?.map(NbtElement::asString) ?: return false).count {  whitelist.contains(it) } == whitelist.size
    }

    private fun handlePlayer(player: PlayerEntity, info: CallbackInfoReturnable<ActionResult>) {
        if(!isWhitelisted(player)) {
            player.sendMessage(Text.Serializer.fromJson("""[{"text":"Whitelisted for: ","color":"red"},{"text":"${whitelist.joinToString(",", transform = String::asUserName)}","color":"green"}]"""), true)
            info.returnValue = ActionResult.SUCCESS
        }
    }

    private fun handleStack(stack: ItemStack) {
        stack.nbt?.getList("whitelist", 8)?.map(NbtElement::asString)?.forEach(whitelist::add)
    }

    fun onBlockUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult, info: CallbackInfoReturnable<ActionResult>) {
        if(!world.isClient)
            getChest(world, pos)?.handlePlayer(player, info)
    }

    fun onBlockPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack, info: CallbackInfo) {
        if(!world.isClient)
            getChest(world, pos)?.handleStack(stack)
    }

    fun onPlacementState(ctx: ItemPlacementContext, info: CallbackInfoReturnable<BlockState>) {
        if(ctx.world.isClient)
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