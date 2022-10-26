package net.orandja.vw.mods.ProtectBlock;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public interface ProtectBlock {

    interface PredicateBlockWithEntity {
        boolean test(World world, BlockPos pos, Entity entity);
    }

    interface PredicateBlock {
        boolean test(World world, BlockPos pos);
    }

    // returns true if the player can destroy the block
    List<PredicateBlockWithEntity> DESTROY = Lists.newArrayList();
    // returns true if the explosion can destroy the block
    List<PredicateBlock> EXPLOSION_PROTECTION = Lists.newArrayList();
    // returns false if the hopper can extract item from block **Might be changed in the future
    List<PredicateBlock> EXTRACTION_PREVENTION = Lists.newArrayList();

    World getWorld();
    List<BlockPos> getAffectedBlocks();
    void setAffectedBlocks(List<BlockPos> list);

    default boolean preventsExtract(World world, BlockPos pos) {
        return EXTRACTION_PREVENTION.stream().anyMatch(predicate -> predicate.test(world, pos));
    }

    default boolean preventsExtract(World world, double x, double y, double z) {
        return preventsExtract(world, new BlockPos(x, y, z));
    }

    default boolean canExplode(World world, BlockPos pos) {
        return EXPLOSION_PROTECTION.stream().noneMatch(predicate -> predicate.test(world, pos));
    }

    default void onExplosion() {
        List<BlockPos> newBlocks = getAffectedBlocks().stream().filter(block -> getWorld() != null && canExplode(getWorld(), block)).toList();
        getAffectedBlocks().clear();
        getAffectedBlocks().addAll(newBlocks);
    }

    default boolean canDestroy(World world, BlockPos pos, Object entity) {
        if(entity instanceof Entity entity1) {
            return DESTROY.stream().anyMatch(predicate -> predicate.test(world, pos, entity1));
        }

        return true;
    }

    default void onPlayerDestroy(World world, BlockPos pos, Object entity, CallbackInfoReturnable<Boolean> info) {
        if(!canDestroy(world, pos, this)) {
            info.setReturnValue(true);
        }
    }

}
