package net.orandja.vw.mods

import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.crafting.ModifierShapedRecipe
import net.orandja.vw.maccessors.ChestBlockEntityMixinAccessor
import net.orandja.vw.utils.VWRecipes
import net.orandja.vw.utils.getOrCompute

class WhitelistedChest_old {
    companion object {
        fun launch() {
            VWRecipes.addRecipe(ModifierShapedRecipe(
                Identifier("vanillawaifu", "whitelisted_chest"), "", 1, 1,
                VWRecipes.getIngredients(arrayOf("C"), mapOf("C" to VWRecipes.singleIngredient("chest")), 1, 1),
                ItemStack(Items.CHEST)
            ) { inventory, player, normalOutput ->
                var output = normalOutput
                println(output.item.translationKey)
                for (i in 0 until inventory.size()) {
                    val craftingStack = inventory.getStack(i)
                    if (!craftingStack.isEmpty && craftingStack.item === Items.CHEST) {
                        val itemStack: ItemStack = output
                        output = craftingStack.copy()
                        output.setCount(itemStack.count)
                        break
                    }
                }

                output.tag = (if (output.hasTag()) output.tag as NbtCompound else NbtCompound()).apply tag@{
                    this.put(
                        "whitelist",
                        (if (this.contains("whitelist")) this.getList(
                            "whitelist",
                            8
                        ) else NbtList()).apply whitelist@{
                            val playerUUID: String = player.uuidAsString
                            if (this.find { element -> element.asString().equals(playerUUID) } != null) {
                                return@whitelist
                            }

                            this.add(NbtString.of(playerUUID))
                            this@tag.put(
                                "display",
                                (if (this@tag.contains("display")) this@tag.get("display") else NbtCompound()).apply display@{
                                    if ((this@display as NbtCompound).contains("Lore")) {
                                        this@display.getOrCompute("Lore", this@display::contains, { id -> this@display.getList(id, 8)}, {
                                            list -> list + """{"text":"— ${player.name.asString()}","color":"green"}"""
                                        }, {
                                            id ->
                                            val list = NbtList()
                                            list + """{"text":"Whitelisted Chest allowed for: ","color":"green"}"""
                                            list
                                        })
                                        this@display.getList("Lore", 8)
                                            .add(NbtString.of("""{"text":"— ${player.name.asString()}","color":"green"}"""))
                                        return@tag
                                    }

                                    (this@display).put("Lore", NbtList().apply lore@{
                                        this@lore + """{"text":"Whitelisted Chest allowed for: ","color":"green"}""" + """{"text":"— ${player.name.asString()}","color":"green"}"""
                                    })
                                })
                        })
                }

                output
            })

            ProtectBlock.DESTROY.add(WhitelistedChest_old::canDestroy)
            ProtectBlock.EXPLOSIONS.add { world, pos -> hasWhitelist(world = world, pos = pos) }
            ProtectBlock.EXTRACTORS.add { world, pos -> hasWhitelist(world = world, pos = pos) }
        }

        fun sameWhitelist(entity1: BlockEntity? = null, entity2: BlockEntity? = null, world: World? = null, pos1: BlockPos? = null, pos2: BlockPos? = null, stackList: NbtList? = null): Boolean {
            val whitelist1 = getWhitelist(entity1 ?: world?.getBlockEntity(pos1)) ?: return false
            val whitelist2 = stackList?.map(NbtElement::asString) ?: getWhitelist(entity2 ?: world?.getBlockEntity(pos2)) ?: return false
            return whitelist2.count(whitelist1::contains) == whitelist2.size
        }

        fun isWhitelisted(entity: BlockEntity? = null, world: World? = null, pos: BlockPos? = null, player: PlayerEntity): Boolean {
            val accessor = (entity ?: world?.getBlockEntity(pos)) as? ChestBlockEntityMixinAccessor ?: return false
            return hasWhitelist(accessor = accessor) && !player.isCreative && accessor.accessWhitelist().contains(player.uuidAsString)
        }

        fun hasWhitelist(entity: BlockEntity? = null, world: World? = null, pos: BlockPos? = null, accessor: ChestBlockEntityMixinAccessor? = null): Boolean {
            val entityAccessor = (accessor ?: entity ?: world?.getBlockEntity(pos)) as? ChestBlockEntityMixinAccessor ?: return false
//            val entityAccessor = entity as? ChestBlockEntityMixinAccessor ?: return false
            return entityAccessor.accessWhitelist().size > 0
        }

        private fun getWhitelist(entity: BlockEntity?): ArrayList<String>? {
            val entityAccessor = entity as? ChestBlockEntityMixinAccessor ?: return null
            return if (hasWhitelist(entity)) entityAccessor.accessWhitelist() else null
        }

        fun canDestroy(world: World, pos: BlockPos, player: Entity): Boolean {
            return !hasWhitelist(world = world, pos = pos) || (player is PlayerEntity && isWhitelisted(world = world, pos = pos, player = player))
        }
//
//        fun sameWhitelist(world: World?, pos: BlockPos?, stackList: NbtList): Boolean {
//            return sameWhitelist(world!!.getBlockEntity(pos), stackList)
//        }

//        fun sameWhitelist(entity: BlockEntity? = null, world: World? = null, pos: BlockPos? = null, stackList: NbtList): Boolean {
//            val entityMixin = (entity ?: world?.getBlockEntity(pos)) as? ChestBlockEntityMixinAccessor ?: return false
//
//            return stackList.map(NbtElement::asString).count(entityMixin.accessWhitelist()::contains) == entityMixin.accessWhitelist().size
//        }

//        fun isWhitelisted(world: World?, pos: BlockPos?, player: PlayerEntity): Boolean {
//            return notNull(world, pos, player) && isWhitelisted(world!!.getBlockEntity(pos)!!, player)
//        }
//
//        fun isWhitelisted(entity: BlockEntity, player: PlayerEntity): Boolean {
//            return !(entity is ChestBlockEntity) || !hasWhitelist(entity) || player.isCreative || (entity as ChestBlockEntityMixinAccessor).accessWhitelist().contains(
//                player.uuidAsString
//            )
//        }

        //        fun hasWhitelist(world: World, pos: BlockPos): Boolean {
//            return hasWhitelist(world.getBlockEntity(pos))
//        }

//        fun sameWhitelist(world: World?, pos1: BlockPos?, pos2: BlockPos?): Boolean {
//            return notNull(world, pos1, pos2) && sameWhitelist(
//                world!!.getBlockEntity(pos1),
//                world!!.getBlockEntity(pos2)
//            )
//        }
    }
}
