package net.orandja.vw.logic2

import net.minecraft.block.BlockState
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
import net.orandja.vw.mods.EnchantMore
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

private fun List<NbtElement>.toNBTList(): NbtList =
    NbtList().apply list@{ this@toNBTList.forEach { this@list.add(it) } }

interface BlockWithEnchantment {

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
            (tag.get("Enchantments") as NbtList).map(EnchantMore::fromNBT).forEach(this::applyEnchantments)
        }
    }

    fun saveEnchantments(tag: NbtCompound) {
        if (this.hasEnchantments()) {
            tag.put("Enchantments", getEnchantments().filter(EnchantMore::validEnchant)
                .map { entry -> EnchantMore.toNBT(entry.key, entry.value) }
                .toNBTList())
        }
    }

    fun onBlockPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack, info: CallbackInfo) {
        if (stack.nbt?.contains("Enchantments") == true) {
            (stack.nbt?.get("Enchantments") as NbtList).map(EnchantMore::fromNBT)
                .forEach((world.getBlockEntity(pos) as BlockWithEnchantment)::applyEnchantments)
        }
    }

    fun enchantDrops(drops: List<ItemStack>, state: BlockState, builder: LootContext.Builder): List<ItemStack> {
        val blockWithEnchantment =
            (builder.get(LootContextParameters.BLOCK_ENTITY) as? BlockWithEnchantment) ?: return drops
        val enchantments = blockWithEnchantment.getEnchantments().filter(EnchantMore::validEnchant)
            .map { entry -> EnchantMore.toNBT(entry.key, entry.value) }
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

}