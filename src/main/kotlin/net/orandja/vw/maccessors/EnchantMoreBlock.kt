//package net.orandja.vw.maccessors
//
//import net.minecraft.block.BlockState
//import net.minecraft.entity.LivingEntity
//import net.minecraft.item.ItemStack
//import net.minecraft.loot.context.LootContext
//import net.minecraft.loot.context.LootContextParameters
//import net.minecraft.nbt.NbtCompound
//import net.minecraft.nbt.NbtList
//import net.minecraft.util.math.BlockPos
//import net.minecraft.world.World
//import net.orandja.vw.mods.EnchantMore
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
//
//interface EnchantMoreBlock {
//
//    fun placeEnchant(
//        world: World,
//        pos: BlockPos,
//        state: BlockState,
//        placer: LivingEntity,
//        stack: ItemStack,
//        info: CallbackInfo
//    ) {
//        if (!stack.hasNbt() || !stack.nbt!!.contains("Enchantments")) {
//            return
//        }
//
//        val entityMixin = world.getBlockEntity(pos) as EnchantMoreAccessor
//        (stack.nbt!!.get("Enchantments") as NbtList).map(EnchantMore::fromNBT)
//            .forEach(entityMixin::applyEnchantments)
//
//    }
//
//    fun applyEnchant(entity: EnchantMoreAccessor, stacks: List<ItemStack>) {
//        val enchantments = NbtList().apply list@{
//            entity.getEnchantments().filter(EnchantMore::validEnchant).forEach { name, level -> this.add(EnchantMore.toNBT(name, level)) }
//        }
//
//        stacks.forEach { droppedStack: ItemStack ->
//            if (!enchantments.isEmpty()) {
//                if (!droppedStack.hasNbt()) {
//                    droppedStack.nbt = NbtCompound()
//                }
//
//                if (droppedStack.nbt!!.contains("Enchantments")) {
//                    enchantments.forEach((droppedStack.nbt!!["Enchantments"] as NbtList)::add)
//                } else {
//                    droppedStack.nbt!!.put("Enchantments", enchantments.copy())
//                }
//            }
//        }
//    }
//
//    fun enchantDrops(drops: List<ItemStack>, state: BlockState, builder: LootContext.Builder): List<ItemStack> {
//        applyEnchant((builder.get(LootContextParameters.BLOCK_ENTITY) as EnchantMoreAccessor), drops)
//        return drops
//    }
//
//}