package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.state.property.Property;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.orandja.vw.logic.EnchantedHopper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mixin(value = HopperBlockEntity.class, priority = 1001)
public abstract class HopperBlockEntityMixin extends LockableContainerBlockEntity implements EnchantedHopper {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At("HEAD"), cancellable = true)
    private static void extract(World world, Hopper hopper, CallbackInfoReturnable<Boolean> info) {
        Companion.preventExtraction(world, hopper, info);
    }

    @Inject(method = "insertAndExtract", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/HopperBlockEntity;setTransferCooldown(I)V"))
    private static void insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> info) {
        Companion.reduceCooldown(blockEntity);
    }

    @Redirect(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;isInventoryEmpty(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Z"))
    private static boolean filterIsInventoryEmpty(Inventory inventory, Direction facing) {
        return Companion.filterIsInventoryEmpty(inventory, facing);
    }

    @Redirect(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;getAvailableSlots(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Ljava/util/stream/IntStream;"))
    private static IntStream filterGetAvailableSlots(Inventory inventory, Direction facing) {
        return Companion.filterGetAvailableSlots(inventory, facing);
    }

    @Redirect(method = "insertAndExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
    private static Comparable<?> redstoneEnabled(BlockState state2, Property<Boolean> value, World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        return Companion.redstoneEnabled(world, pos, state, blockEntity, booleanSupplier);
    }

    @Getter @Setter private boolean addOffset = false;
    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputInventory(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Lnet/minecraft/inventory/Inventory;"))
    private static void enableOffset(World world, Hopper hopper, CallbackInfoReturnable<Boolean> info) {
        Companion.setAddOffset(hopper, true);
    }
    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;"))
    private static void disableOffset(World world, Hopper hopper, CallbackInfoReturnable<Boolean> info) {
        Companion.setAddOffset(hopper, false);
    }

    @Redirect(method = "getInputItemEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShape;getBoundingBoxes()Ljava/util/List;"))
    private static List<Box> expandSweeping(VoxelShape instance, World world, Hopper hopper) {
        return Companion.expandSweeping(instance.getBoundingBoxes(), world, hopper);
    }

    @Inject(method = "getInputItemEntities", at = @At("HEAD"), cancellable = true)
    private static void knockbackInputEntites(World world, Hopper instance, CallbackInfoReturnable<List<ItemEntity>> info) {
        if(instance instanceof EnchantedHopper hopper && hopper.getKnockback() > 0) {
            info.setReturnValue(Companion.inputZones(instance).getBoundingBoxes().stream().flatMap(box -> world.getEntitiesByClass(ItemEntity.class, Companion.offsetCollectZone(box, instance), EntityPredicates.VALID_ENTITY).stream()).collect(Collectors.toList()));
        }
    }

    @Inject(method = "onEntityCollided", at = @At("HEAD"), cancellable = true)
    private static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, HopperBlockEntity blockEntity, CallbackInfo info) {
        if(blockEntity instanceof EnchantedHopper hopper && hopper.getKnockback() > 0) {
            info.cancel();
        }
    }


    @Override public Inventory getOutInventory(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state) {return getOutputInventory(world, pos, state);}

    @Override public boolean isInvFull(@NotNull Inventory inventory, @NotNull Direction direction) {return isInventoryFull(inventory, direction);}

    @Override public @NotNull IntStream getOutputAvailableSlots(@NotNull Inventory inventory, @NotNull Direction side) {return getAvailableSlots(inventory, side);}

    @Shadow private static native Inventory getOutputInventory(World world, BlockPos pos, BlockState state);
    @Shadow private static native boolean isInventoryFull(Inventory inventory, Direction direction);
    @Shadow private static native IntStream getAvailableSlots(Inventory inventory, Direction side);

    @Shadow private native boolean isFull();

    @Override public boolean isHopperFull() {return isFull();}

    @Override public boolean isHopperEmpty() {return isEmpty();}

    @Override public void markHopperDirty(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state) {markDirty(world, pos, state);}

    @Shadow public native void setTransferCooldown(int cooldown);

    @Override public void setHopperCooldown(int cooldown) {setTransferCooldown(cooldown);}

    @Shadow protected native DefaultedList<ItemStack> getInvStackList();

    @Override public DefaultedList<ItemStack> getInvList() {return getInvStackList();}

    @Getter @Setter short mending = 0;
    @Getter @Setter short silkTouch = 0;
    @Getter @Setter short efficiency = 0;
    @Getter @Setter short knockback = 0;
    @Getter @Setter short sweeping = 0;
    @Getter @Shadow private int transferCooldown;

    @Shadow public abstract double getHopperX();

    @Shadow public abstract double getHopperY();
    @Inject(method = "getHopperY", at = @At("HEAD"), cancellable = true)
    public void offsetHopperY(CallbackInfoReturnable<Double> info) {
        info.setReturnValue((double)this.pos.getY() + 0.5 + (addOffset ? Companion.hopperOffset(this) : 0));
    }

    @Shadow public abstract double getHopperZ();

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound tag, CallbackInfo info) {
        loadEnchantments(tag);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void writeNbt(NbtCompound tag, CallbackInfo info) {
        saveEnchantments(tag);
    }

    @Override public double getPosX() { return this.getHopperX(); }
    @Override public double getPosY() { return this.getHopperY(); }
    @Override public double getPosZ() { return this.getHopperZ(); }
}
