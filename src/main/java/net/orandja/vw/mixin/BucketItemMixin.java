package net.orandja.vw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.orandja.vw.logic.InfinityBucket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item implements FluidModificationItem, InfinityBucket {
    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsage;exchangeStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack onEmptyBucket(ItemStack inputStack, PlayerEntity player, ItemStack outputStack) {
        return Companion.handleEmptyInfinityBucket(inputStack, player, outputStack);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BucketItem;getEmptiedStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack onWaterBucket(ItemStack stack, PlayerEntity player) {
        return Companion.handleWaterInfinityBucket(stack, player);
    }

}
