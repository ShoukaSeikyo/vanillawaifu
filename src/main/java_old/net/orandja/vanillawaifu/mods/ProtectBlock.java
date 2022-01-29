package net.orandja.vanillawaifu.mods;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.VanillaWaifu;

/**
 * Related Mixins:
 * net.orandja.vanillawaifu.mixin.ExplosionMixin
 * net.orandja.vanillawaifu.mixin.HopperBlockEntityMixin
 * net.orandja.vanillawaifu.mixin.PlayerEntityMixin
 * */
public class ProtectBlock {

    public interface ProtectDestruction {
        boolean canDestroy(World world, BlockPos pos, Object entity);
    }

    public interface ProtectExtract {
        boolean preventExtract(World world, BlockPos pos);
    }

    public static boolean canDestroy(World world, BlockPos pos, Object entity) {
        for (ProtectDestruction protector : VanillaWaifu.PROTECTORS) {
            if(protector.canDestroy(world, pos, entity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean preventExtract(World world, BlockPos pos) {
        for (ProtectExtract protector : VanillaWaifu.HOPPER_EXTRACT) {
            if(protector.preventExtract(world, pos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean preventExtract(World world, double x, double y, double z) {
        return preventExtract(world, new BlockPos(x, y, z));
    }
}
