package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.orandja.vw.maccessors.BarrelInventoryAccessor;
import net.orandja.vw.logic.DeepBarrelLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.orandja.vw.utils.ItemUtilsKt.areStacksCompatible;

@Mixin(BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin extends LootableContainerBlockEntity implements BarrelInventoryAccessor, DeepBarrelLogic {

    @Getter @Setter short infinity = 0;
    @Getter @Setter short efficiency = 0;
    @Shadow @Getter @Setter private DefaultedList<ItemStack> inventory;

    protected BarrelBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
    public void readNbt(NbtCompound tag, CallbackInfo info) {
        this.readNbt(tag, info, super::readNbt);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"), cancellable = true)
    public void writeNbt(NbtCompound tag, CallbackInfo info) {
        this.writeNbt(tag, info, super::writeNbt);
    }

    @Inject(method = "size", cancellable = true, at = @At("HEAD"))
    public void size(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(this.inventory.size());
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return areStacksCompatible(this.inventory.get(0), stack);
    }

    @Inject(method = "createScreenHandler", at = @At("HEAD"), cancellable = true)
    protected void createScreenHandler(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> info) {
        this.createBarrelScreenHandler(this, syncId, playerInventory, info);
    }
}
