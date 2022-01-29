package net.orandja.vw.mods

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.logic2.EnchantedBrewingStand

class EnchantMore {
    companion object {
        var ENCHANTS: HashMap<Item, (Enchantment, ItemStack) -> Boolean> = HashMap()

        fun launch() {
            addBasic(
                item = Items.FURNACE,
                Enchantments.SMITE,
                Enchantments.EFFICIENCY,
                Enchantments.FORTUNE,
                Enchantments.FLAME,
                Enchantments.UNBREAKING,
                Enchantments.FIRE_ASPECT
            )

            addBasic(
                item = Items.SMOKER,
                Enchantments.SMITE,
                Enchantments.EFFICIENCY,
                Enchantments.FORTUNE,
                Enchantments.FLAME,
                Enchantments.UNBREAKING,
                Enchantments.FIRE_ASPECT
            )


            addBasic(
                item = Items.BLAST_FURNACE,
                Enchantments.SMITE,
                Enchantments.EFFICIENCY,
                Enchantments.FORTUNE,
                Enchantments.FLAME,
                Enchantments.UNBREAKING,
                Enchantments.FIRE_ASPECT
            )


            addBasic(
                item = Items.BREWING_STAND,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.EFFICIENCY,
                Enchantments.FIRE_ASPECT,
                Enchantments.UNBREAKING,
                Enchantments.SILK_TOUCH
            )

            addBasic(
                item = Items.HOPPER,
                Enchantments.EFFICIENCY,
                Enchantments.SILK_TOUCH,
                Enchantments.MENDING
            )

            addBasic(
                item = Items.BUCKET,
                Enchantments.INFINITY
            )

            addComplex(
                item = Items.BARREL
            ) { enchantment, stack ->
                if(stack.count > 1) {
                    return@addComplex false
                }

                when (enchantment) {
                    Enchantments.INFINITY -> return@addComplex true
                    Enchantments.EFFICIENCY -> return@addComplex EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0 && stack.hasEnchantments();
                }

                return@addComplex false
            }

            ProtectBlock.EXTRACTORS.add(EnchantMore::preventExtractBrewingStand)
        }

        fun preventExtractBrewingStand(world: World, pos: BlockPos): Boolean {
            val brewingStand = world.getBlockEntity(pos) as? EnchantedBrewingStand ?: return false
            val brewTime = brewingStand.brewTime as? Int ?: 0

            return brewingStand.hasEnchantment(Enchantments.SILK_TOUCH) && brewTime > 0
        }

        @JvmStatic
        fun canEnchant(stack: ItemStack, enchantment: Enchantment): Boolean {
            return ENCHANTS.containsKey(stack.item) && ENCHANTS[stack.item]!!.invoke(enchantment, stack)
        }

        fun addBasic(item: Item, vararg enchantments: Enchantment, limit: Int = 1) {
            ENCHANTS[item] = { enchantment, stack -> stack.count <= limit && enchantments.contains(enchantment) }
        }

        fun addComplex(item: Item, complex: (Enchantment, ItemStack) -> Boolean) {
            ENCHANTS[item] = complex
        }

        fun toNBT(name: String, level: Short): NbtCompound {
            val compound = NbtCompound()
            compound.putShort("lvl", level)
            compound.putString("id", "minecraft:$name")
            return compound
        }

        fun nbtToList(list: NbtList): (String, Short) -> Unit {
            return { name: String, lvl: Short ->
                list.add(toNBT(name, lvl))
            }
        }

        fun validEnchant(entry: Map.Entry<String, Short>): Boolean {
            return entry.value > 0
        }

        fun fromNBT(element: NbtElement, removeNamespace: Boolean = true): Pair<String, Short> {
            val tag = element as NbtCompound
            val id = if (removeNamespace) tag.getString("id").replace("minecraft:", "") else tag.getString("id")
            return Pair(id, tag.getShort("lvl"))
        }
    }
}