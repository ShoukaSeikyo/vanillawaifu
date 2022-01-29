package net.orandja.vw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.orandja.vw.logic.InfinityBucketLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin extends Item implements FluidModificationItem, InfinityBucketLogic {
    public BucketItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsage;exchangeStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack exchangeStack(ItemStack inputStack, PlayerEntity player, ItemStack outputStack) {
        return onStackChange(inputStack, player, outputStack);
    }

}
