package net.orandja.vanillawaifu.mods;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.orandja.vanillawaifu.VanillaWaifu;
import net.orandja.vanillawaifu.utils.QuickUtils;

import java.util.List;
import java.util.Map;

public class CloudBox {

    public static Map<String, DefaultedList<ItemStack>> CLOUD_BOXES;

    public static void init(MinecraftServer server) {
        CLOUD_BOXES = Maps.newHashMap();
        VanillaWaifu.WORLD_DATA.computeIfAbsent(server.getOverworld().getRegistryKey().getValue(), (_ignored) -> Maps.newHashMap()).put("cloudbox", CloudBox::save);
    }

    private static void save(CompoundTag compoundTag) {
        CLOUD_BOXES.forEach((name, inventory) -> compoundTag.put(name, QuickUtils.create(new CompoundTag(), (invTag) -> Inventories.toTag(invTag, inventory))));
    }
}
