package net.orandja.vw.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.DoubleTool;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin extends MiningToolItem implements DoubleTool {

    protected ShovelItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        return useDoubleShovel(super.postMine(stack, world, state, pos, miner), stack, world, state, pos, miner, ShovelItem.class);
    }
}
