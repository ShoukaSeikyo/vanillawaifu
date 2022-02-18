package net.orandja.vw.logic

import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.mcutils.toNBTList
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

interface BlockWithEnchantment {

    //BlockWithEntity

    fun getEnchantments(): Map<String, Short>
    fun hasEnchantments(): Boolean
    fun hasEnchantment(enchantment: Enchantment, level: Short = 1): Boolean {
        return false
    }

    fun applyEnchantments(name: String, level: Short)
    fun applyEnchantments(pair: Pair<String, Short>) {
        applyEnchantments(pair.first, pair.second)
    }

    fun loadEnchantments(tag: NbtCompound) {
        if (tag.contains("Enchantments")) {
            (tag.get("Enchantments") as NbtList).map(::fromNBT).forEach(this::applyEnchantments)
        }
    }

    fun saveEnchantments(tag: NbtCompound) {
        if (this.hasEnchantments()) {
            tag.put("Enchantments", getEnchantments().filter(::validEnchant)
                .map { toNBT(it.key, it.value) }
                .toNBTList())
        }
    }

    fun onBlockPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack, info: CallbackInfo) {
        if (stack.nbt?.contains("Enchantments") == true) {
            (stack.nbt?.get("Enchantments") as NbtList).map(::fromNBT)
                .forEach {
                    if(state.block is BlockWithEntity)
                        (world.getBlockEntity(pos) as BlockWithEnchantment).applyEnchantments(it)
                }
        }
    }

    fun enchantDrops(drops: List<ItemStack>, state: BlockState, builder: LootContext.Builder): List<ItemStack> {
        val blockWithEnchantment =
            (builder.get(LootContextParameters.BLOCK_ENTITY) as? BlockWithEnchantment) ?: return drops
        val enchantments = blockWithEnchantment.getEnchantments().filter(::validEnchant)
            .map { entry -> toNBT(entry.key, entry.value) }
            .toNBTList()

        drops.forEach {
            if (!enchantments.isEmpty()) {
                if (it.orCreateNbt.contains("Enchantments"))
                    enchantments.forEach((it.orCreateNbt["Enchantments"] as NbtList)::add)
                else
                    it.orCreateNbt.put("Enchantments", enchantments.copy())
            }
        }

        return drops
    }

    fun toNBT(name: String, level: Short): NbtCompound {
        val compound = NbtCompound()
        compound.putShort("lvl", level)
        compound.putString("id", "minecraft:$name")
        return compound
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