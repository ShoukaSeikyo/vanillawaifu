package net.orandja.vanillawaifu.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.utils.QuickUtils;

import java.util.List;
import java.util.Map;

public abstract class WaifuBlockEntity implements WaifuWorldData {

    private static Map EMPTY_MAP = ImmutableMap.of();
    private static List EMPTY_LIST = ImmutableList.of();

    private static Map<Identifier, Map<Class<? extends WaifuBlockEntity>, List<WaifuBlockEntity>>> REGISTRY_BY_CLASS = Maps.newHashMap();
    private static Map<Identifier, Map<BlockPos, WaifuBlockEntity>> REGISTRY_BY_POS = Maps.newHashMap();
    public static void register(World world, BlockPos pos, WaifuBlockEntity waifuBlockEntity) {
        REGISTRY_BY_CLASS.computeIfAbsent(world.getRegistryKey().getValue(), ignored -> Maps.newHashMap()).computeIfAbsent(waifuBlockEntity.getClass(), ignored -> Lists.newArrayList()).add(waifuBlockEntity);
        REGISTRY_BY_POS.computeIfAbsent(world.getRegistryKey().getValue(), ignored -> Maps.newHashMap()).put(pos, waifuBlockEntity);
    }

    public static <T extends WaifuBlockEntity> T get(Class<T> clazz, World world, BlockPos pos) {
        return QuickUtils.castOrNull(REGISTRY_BY_CLASS.getOrDefault(world.getRegistryKey().getValue(), EMPTY_MAP).get(pos), clazz);
    }

    public static <T extends WaifuBlockEntity> List<T> getAll(Class<T> clazz, World world) {
        return (List<T>) REGISTRY_BY_CLASS.getOrDefault(world.getRegistryKey().getValue(), EMPTY_MAP).getOrDefault(clazz, EMPTY_LIST);
    }

    protected static void read(NbtCompound tag) {
        tag.getKeys().forEach(clazzName -> read(clazzName, (NbtList) tag.get(clazzName)));
    }
    protected static void read(String clazzName, NbtList tag) {
        Class clazz = QuickUtils.noCatch(() -> Class.forName("net.orandja.vanillawaifu.world.blockentity." + clazzName));
        tag.forEach(compoundTag -> {
            ((WaifuBlockEntity) QuickUtils.noCatch(() -> clazz.getDeclaredConstructor().newInstance())).fromNBT((NbtCompound) compoundTag);
        });
    }

    public World world;
    public BlockPos pos;

    public WaifuBlockEntity setWorld(World world) {
        this.world = world;
        return this;
    }

    public WaifuBlockEntity setPosition(BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public void toNBT(NbtCompound tag) {
        tag.putInt("x", this.pos.getX());
        tag.putInt("y", this.pos.getY());
        tag.putInt("z", this.pos.getZ());
    }

    @Override
    public void fromNBT(NbtCompound tag) {
        this.setPosition(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }
}
