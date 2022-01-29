package net.orandja.vanillawaifu.utils;

import com.google.common.collect.Maps;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.world.WaifuBlockEntity;
import net.orandja.vanillawaifu.world.WaifuWorldData;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class WorldUtils {

//    public static Map<Identifier, Map<Class<? extends WorldDataObject>, WorldDataObject>> worldData = Maps.newHashMap();
//    public static <T extends WaifuWorldData, K extends WorldDataObject> Map<Class<T>, K> getMap(World world, Class<T> clazz, WorldDataObject<K> constructor) {
//        if(!worldData.containsKey(world.getRegistryKey().getValue())) {
//            worldData.put(world.getRegistryKey().getValue(), Maps.newHashMap());
//        }
//
//        Map<Class<? extends WorldDataObject>, WorldDataObject> maps = worldData.get(world.getRegistryKey().getValue());
//        if(maps.containsKey(clazz)) {
//            maps.put(clazz, (K) constructor.create());
//        }
//        return (K) worldData.computeIfAbsent(world.getRegistryKey().getValue(), identifier -> Maps.newHashMap()).get(clazz);
//    }
//    public static WaifuBlockEntity getBlockEntity(World world, BlockPos pos) {
//        WorldUtils.<WaifuBlockEntity, Map<BlockPos, WaifuBlockEntity>>getMap(world, WaifuBlockEntity.class).
//    }
}
