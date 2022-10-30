package net.orandja.vw.mods.BlockWithEnchantment;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.mcutils.NBTUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface BlockWithEnchantment {

    Map<String, Short> getEnchantments();
    default Stream<Map.Entry<String, Short>> getEnchantmentsStream() {
        return getEnchantments().entrySet().stream();
    }
    boolean hasEnchantments();
    default boolean hasEnchantment(Enchantment enchantment) {
        return hasEnchantment(enchantment, (short) 1);
    }
    default boolean hasEnchantment(Enchantment enchantment, short level) {
        return false;
    }

    void applyEnchantment(String name, short level);
    default void applyEnchantment(Pair<String, Short> pair) {
        applyEnchantment(pair.getLeft(), pair.getRight());
    }

    default void loadEnchantments(NbtCompound tag) {
        if(tag.contains("Enchantments")) {
            tag.getList("Enchantments", NbtElement.COMPOUND_TYPE).stream().map(this::fromNBT).forEach(this::applyEnchantment);
        }
    }

    default void saveEnchantments(NbtCompound tag) {
        if(this.hasEnchantments()) {
            tag.put("Enchantments", NBTUtils.toNbtList(getEnchantmentsStream().filter(this::validEnchantment).map(this::toNBT)));
        }
    }

    default void onBlockPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if(stack.hasNbt() && stack.getNbt().contains("Enchantments")) {
            stack.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE).stream().map(this::fromNBT).forEach(it -> {
                if(state.getBlock() instanceof BlockWithEntity && world.getBlockEntity(pos) instanceof BlockWithEnchantment blockWithEnchantment) {
                    blockWithEnchantment.applyEnchantment(it);
                }
            });
        }
    }

    default List<ItemStack> enchantLoots(List<ItemStack> drops, BlockState state, LootContext.Builder builder) {
        if(builder.getNullable(LootContextParameters.BLOCK_ENTITY) instanceof BlockWithEnchantment blockWithEnchantment) {
            NbtList enchantments = NBTUtils.toNbtList(blockWithEnchantment.getEnchantmentsStream().filter(this::validEnchantment).map(this::toNBT));

            if(!enchantments.isEmpty()) {
                drops.forEach(it -> {
                    if(it.getOrCreateNbt().contains("Enchantments")) {
                        it.getOrCreateNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE).addAll(enchantments);
                    } else {
                        it.getOrCreateNbt().put("Enchantments", enchantments.copy());
                    }
                });
            }
        }

        return drops;
    }

    default NbtCompound toNBT(Map.Entry<String, Short> entry) {
        return toNBT(entry.getKey(), entry.getValue());
    }
    default NbtCompound toNBT(String name, short level) {
        NbtCompound tag = new NbtCompound();
        tag.putShort("lvl", level);
        tag.putString("id", name);
        return tag;
    }

    default boolean validEnchantment(Map.Entry<String, Short> entry) {
        return entry.getValue() > 0;
    }

    default Pair<String, Short> fromNBT(NbtElement element) {
        return fromNBT(element, false);
    }

    default Pair<String, Short> fromNBT(NbtElement element, boolean removeNamespace) {
        NbtCompound tag = (NbtCompound) element;
        String id = removeNamespace ? tag.getString("id").replace("minecraft:", "") : tag.getString("id");
        return new Pair(id, tag.getShort("lvl"));
    }
}
