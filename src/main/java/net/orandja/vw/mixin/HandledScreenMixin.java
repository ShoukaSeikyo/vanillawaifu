package net.orandja.vw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.orandja.vw.logic.InventoryMiddleClick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> implements InventoryMiddleClick {

    @Shadow
    private ItemStack cursorStack;

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        checkMiddleClick(cursorStack, slotIndex, button, actionType, player, ci);
    }
}