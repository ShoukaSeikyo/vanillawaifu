package net.orandja.vw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.orandja.vw.logic.CustomRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin extends Slot implements CustomRecipe {
    @Shadow @Final private CraftingInventory input;

    @Shadow @Final private PlayerEntity player;
    private CraftingRecipe recipe = null;

    public CraftingResultSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Redirect(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingInventory;removeStack(II)Lnet/minecraft/item/ItemStack;"))
    public ItemStack onTakeItem(CraftingInventory instance, int slot, int amount) {
        return this.interceptOnTakeItem(recipe, input, this.player, slot, amount);
//        return this.input.removeStack(slot , 1);
    }

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    public void onTakeItemHead(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        Optional<CraftingRecipe> optional = player.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, player.world);
        this.recipe = optional.orElse(null);
    }
//
//    @Inject(method = "onTakeItem", at @At("RETURN"))
//    public void onTakeItemReturn(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
//        this.recipe = null;
//    }

}