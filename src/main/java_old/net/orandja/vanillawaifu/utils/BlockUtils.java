package net.orandja.vanillawaifu.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.VanillaWaifu;

import java.util.function.Consumer;

public class BlockUtils {

    public static <T> void observe(Object blockEntity, String key, Consumer<T> consumer) {
        if (!(blockEntity instanceof BlockEntity))
            return;

        VanillaWaifu.BLOCKENTITY_CONSUMERS.computeIfAbsent(blockEntity, unused -> Maps.newHashMap())
                .computeIfAbsent(key, key1 -> Lists.newArrayList())
                .add(consumer);
    }

    public static <T> void notify(World world, BlockPos pos, String key, T value) {
        notify(world.getBlockEntity(pos), key, value);
    }

    public static <T> void notify(Object blockEntity, String key, T value) {
        if (!(blockEntity instanceof BlockEntity) ||
                !VanillaWaifu.BLOCKENTITY_CONSUMERS.containsKey(blockEntity) ||
                !VanillaWaifu.BLOCKENTITY_CONSUMERS.get(blockEntity).containsKey(key))
            return;

        VanillaWaifu.BLOCKENTITY_CONSUMERS.get(blockEntity).get(key).forEach(consumer -> consumer.accept(value));
    }

    public interface accessorInterface<T> {
        T get();
    }

    public static <T> void accessor(Object blockEntity, String key, accessorInterface<T> accessor) {
        if (!(blockEntity instanceof BlockEntity))
            return;

        VanillaWaifu.BLOCKENTITY_ACCESSORS.computeIfAbsent(blockEntity, unused -> Maps.newHashMap()).put(key, accessor);
    }

    public static <T> T access(World world, BlockPos pos, String key) {
        return access(world.getBlockEntity(pos), key);
    }

    public static <T> T access(Object blockEntity, String key) {
        if (hasAccessor(blockEntity, key))
            return (T) VanillaWaifu.BLOCKENTITY_ACCESSORS.get(blockEntity).get(key).get();

        return null;
    }

    public static boolean hasAccessor(World world, BlockPos pos, String key) {
        return hasAccessor(world.getBlockEntity(pos), key);
    }

    public static boolean hasAccessor(Object blockEntity, String key) {
        return blockEntity instanceof BlockEntity && VanillaWaifu.BLOCKENTITY_ACCESSORS.containsKey(blockEntity) && VanillaWaifu.BLOCKENTITY_ACCESSORS.get(blockEntity).containsKey(key);
    }
}
