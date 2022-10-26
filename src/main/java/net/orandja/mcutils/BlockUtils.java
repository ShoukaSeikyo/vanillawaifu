package net.orandja.mcutils;

import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;

public abstract class BlockUtils {

    public static boolean isOre(Block block) {
        return block instanceof OreBlock || block instanceof RedstoneOreBlock;
    }

    public static boolean isOreAndTheSame(Block block1, Block block2) {
        return isOre(block1) && block2 == block1;
    }

    public static boolean isOre(BlockState state) {
        return isOre(state.getBlock());
    }

    public static boolean isOreAndTheSame(BlockState state1, BlockState state2) {
        return isOreAndTheSame(state1.getBlock(), state2.getBlock());
    }

    public static boolean isOreAndTheSame(BlockState state, Block block) {
        return isOreAndTheSame(state.getBlock(), block);
    }

    public static boolean isWood(BlockState state) {
        return state.isIn(BlockTags.LOGS) || state.getMaterial() == Material.NETHER_WOOD;
    }

    public static boolean isWoodAndTheSame(BlockState state1, BlockState state2) {
        return isWood(state1) && state1.getBlock() == state2.getBlock();
    }

}
