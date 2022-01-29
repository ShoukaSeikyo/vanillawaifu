package net.orandja.vanillawaifu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.orandja.vanillawaifu.mods.EnchantBlocks;
import net.orandja.vanillawaifu.mods.ProtectBlock;
import net.orandja.vanillawaifu.mods.WhitelistedChest;
import net.orandja.vanillawaifu.utils.BlockUtils;
import net.orandja.vanillawaifu.utils.RecipeUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class VanillaWaifu implements ModInitializer {
    public static final String namespace = "vanillawaifu";
    public static MinecraftServer server;
    public static Map<Object, Map<String, List<Consumer>>> BLOCKENTITY_CONSUMERS;
    public static Map<Object, Map<String, BlockUtils.accessorInterface>> BLOCKENTITY_ACCESSORS;
    private static AtomicBoolean started = new AtomicBoolean(false);

    public static Map<Item, List<Enchantment>> ACCEPTED_ENCHANTS;
    public static List<ProtectBlock.ProtectDestruction> PROTECTORS;
    public static List<ProtectBlock.ProtectExtract> HOPPER_EXTRACT;

    public static Map<Identifier, Map<String, Consumer<NbtCompound>>> WORLD_DATA;

    @Override
    public void onInitialize() {
        ACCEPTED_ENCHANTS = Maps.newHashMap();
        BLOCKENTITY_CONSUMERS = Maps.newHashMap();
        BLOCKENTITY_ACCESSORS = Maps.newHashMap();
        PROTECTORS = Lists.newArrayList();
        HOPPER_EXTRACT = Lists.newArrayList();
        WORLD_DATA = Maps.newHashMap();
        started.set(false);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (started.get()) {
                return;
            }

            started.set(true);
            VanillaWaifu.server = server;
            RecipeUtils.init(server);
            EnchantBlocks.init(server);
            WhitelistedChest.init(server);
        });
    }
}
