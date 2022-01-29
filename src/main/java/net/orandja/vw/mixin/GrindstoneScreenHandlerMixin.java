package net.orandja.vw.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.orandja.vw.crafting.TransferEnchantOutputSlot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    Inventory input;

    @Shadow
    Inventory result;

    TransferEnchantOutputSlot outputSlot;

    PlayerInventory playerInventory;

    @Inject(at = @At("RETURN"), method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
    public void init(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context, CallbackInfo info) {
        this.playerInventory = playerInventory;

        this.replaceSlot(new Slot(this.input, 0, 49, 19) {
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments() || stack.isOf(Items.BOOK);
            }
        }, 0);

        this.replaceSlot(new Slot(this.input, 1, 49, 40) {
            public boolean canInsert(ItemStack stack) {
                return stack.isDamageable() || stack.isOf(Items.ENCHANTED_BOOK) || stack.hasEnchantments() || stack.isOf(Items.BOOK);
            }
        }, 1);

        this.replaceSlot(outputSlot = new TransferEnchantOutputSlot(input, result, context), 2);
    }

    public void replaceSlot(Slot slot, int id) {
        slot.id = id;
        this.slots.set(id, slot);
    }

    @Inject(at = @At("HEAD"), method = "updateResult", cancellable = true)
    public void updateResult(CallbackInfo info) {
        outputSlot.getBookAndToolSlots((bookSlot, toolSlot) -> {
            if(this.playerInventory.player.experienceLevel < TransferEnchantOutputSlot.Companion.getCost()) {
                this.result.setStack(0, ItemStack.EMPTY);
            } else {
                ItemStack outputStack = new ItemStack(Items.ENCHANTED_BOOK, 1);
                ItemStack toolStack = input.getStack(toolSlot);

                outputStack.getOrCreateNbt().put("StoredEnchantments", toolStack.getNbt().get("Enchantments"));
                this.result.setStack(0, outputStack);
            }

            this.sendContentUpdates();
            info.cancel();

            return false;
        });
    }
}
