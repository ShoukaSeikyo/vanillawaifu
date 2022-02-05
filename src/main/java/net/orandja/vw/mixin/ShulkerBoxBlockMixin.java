package net.orandja.vw.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.CloudShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
abstract class ShulkerBoxBlockMixin extends BlockWithEntity implements CloudShulkerBox {

    protected ShulkerBoxBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        clearCloud(world, pos);
    }

    @Inject(at = @At("RETURN"), method = "onPlaced")
    void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo info) {
        channelCloud(world, pos, stack);
    }

    @Inject(at = @At("HEAD"), method = "getDroppedStacks", cancellable = true)
    public void getDroppedStacks(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> info) {
        checkCloud(super::getDroppedStacks, state, builder, info);
    }
}