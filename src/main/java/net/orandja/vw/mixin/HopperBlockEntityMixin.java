package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
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
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mixin(value = HopperBlockEntity.class, priority = 1001)
public abstract class HopperBlockEntityMixin extends LockableContainerBlockEntity implements EnchantedHopper {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", at = @At("HEAD"), cancellable = true)
    private static void extract(World world, Hopper hopper, CallbackInfoReturnable<Boolean> info) {
        Companion.preventExtraction(world, hopper, info);
    }

    @Inject(method = "insertAndExtract", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/HopperBlockEntity;setCooldown(I)V"))
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

    @ModifyConstant(method = "getInputInventory", constant = @Constant(doubleValue = 1.0))
    private static double offsetKnockback(double constant, World world, Hopper hopper) {
        return Companion.offsetKnockback(world, hopper);
    }

    @Redirect(method = "getInputItemEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/shape/VoxelShape;getBoundingBoxes()Ljava/util/List;"))
    private static List<Box> expandSweeping(VoxelShape instance, World world, Hopper hopper) {
        return Companion.expandSweeping(instance.getBoundingBoxes(), world, hopper);
    }

    @Redirect(method = "getInputItemEntities", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private static Stream<? super Box> offsetItemZone(Stream<Box> instance, Function<Box, Stream<ItemEntity>> function, World world, Hopper hopper) {
        return instance.flatMap(box -> world.getEntitiesByClass(ItemEntity.class, box.offset(hopper.getHopperX() - 0.5, Companion.offsetItemZone(hopper), hopper.getHopperZ() - 0.5), EntityPredicates.VALID_ENTITY).stream());
    }

    @Shadow
    private static native Inventory getOutputInventory(World world, BlockPos pos, BlockState state);

    @Override
    public Inventory getOutInventory(@NotNull World world, @NotNull BlockPos pos, @NotNull BlockState state) {
        return getOutputInventory(world, pos, state);
    }

    @Shadow
    private static native boolean isInventoryFull(Inventory inventory, Direction direction);

    @Override
    public boolean isInvFull(Inventory inventory, Direction direction) {
        return isInventoryFull(inventory, direction);
    }


    @Shadow
    private static native IntStream getAvailableSlots(Inventory inventory, Direction side);

    public @NotNull IntStream getOutputAvailableSlots(Inventory inventory, Direction side) {
        return getAvailableSlots(inventory, side);
    }

    @Shadow
    private native boolean isFull();

    @Override
    public boolean isHopperFull() {
        return isFull();
    }

    @Override
    public boolean isHopperEmpty() {
        return isEmpty();
    }

    @Override
    public void markHopperDirty(World world, BlockPos pos, BlockState state) {
        markDirty(world, pos, state);
    }

    @Shadow
    private native void setCooldown(int cooldown);

    @Override
    public void setHopperCooldown(int cooldown) {
        setCooldown(cooldown);
    }

    @Shadow
    protected native DefaultedList<ItemStack> getInvStackList();

    @Override
    public DefaultedList<ItemStack> getInvList() {
        return getInvStackList();
    }

    @Getter @Setter short mending = 0;
    @Getter @Setter short silkTouch = 0;
    @Getter @Setter short efficiency = 0;
    @Getter @Setter short knockback = 0;
    @Getter @Setter short sweeping = 0;
    @Getter @Setter @Shadow private int transferCooldown;

    @Shadow public abstract double getHopperX();

    @Shadow public abstract double getHopperY();

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
