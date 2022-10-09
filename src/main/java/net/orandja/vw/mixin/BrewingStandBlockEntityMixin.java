package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.EnchantedBrewingStand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin implements EnchantedBrewingStand {

    @Shadow @Getter @Setter int brewTime;
    @Shadow @Getter @Setter int fuel;
    @Shadow @Getter @Setter private DefaultedList<ItemStack> inventory;
    @Getter @Setter short baneOfArthropods = 0;
    @Getter @Setter short efficiency = 0;
    @Getter @Setter short unbreaking = 0;
    @Getter @Setter short fireAspect = 0;
    @Getter @Setter short silkTouch = 0;

    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;fuel:I"))
    private static void setFuelCount(BrewingStandBlockEntity blockEntity, int fuel) {
        Companion.setFuelCount(blockEntity, fuel);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;brewTime:I"))
    private static void accelerate(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo info) {
        Companion.accelerate(blockEntity);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound tag, CallbackInfo info) {
        loadEnchantments(tag);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void writeNbt(NbtCompound tag, CallbackInfo info) {
        saveEnchantments(tag);
    }
}
