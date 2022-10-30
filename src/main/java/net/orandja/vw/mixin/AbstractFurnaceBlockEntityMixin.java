package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.EnchantedFurnaceBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends LockableContainerBlockEntity implements EnchantedFurnaceBlock {
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Getter @Setter short smite = 0;
    @Getter @Setter short efficiency = 0;
    @Getter @Setter short flame = 0;
    @Getter @Setter short unbreaking = 0;
    @Getter @Setter short fireAspect = 0;
    @Getter @Setter short fortune = 0;
    @Shadow @Getter @Setter int burnTime;
    @Shadow @Getter @Setter int fuelTime;
    @Shadow @Getter @Setter int cookTime;
    @Shadow @Getter @Setter int cookTimeTotal;
    @Shadow @Getter @Setter protected DefaultedList<ItemStack> inventory;
    @Shadow protected abstract boolean isBurning();

    public boolean burning() {
        return isBurning();
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound tag, CallbackInfo info) {
        this.loadEnchantments(tag);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void writeNbt(NbtCompound tag, CallbackInfo info) {
        this.saveEnchantments(tag);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I"))
    private static void decreaseBurnTime(AbstractFurnaceBlockEntity entity, int ignored) {
        EnchantedFurnaceBlock.Companion.decreaseBurnTime(entity);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1, target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I"))
    private static void setBurnTime(AbstractFurnaceBlockEntity entity, int burnTime) {
        EnchantedFurnaceBlock.Companion.setBurnTime(entity, burnTime);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;cookTime:I"))
    private static void accelerateCookTime(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity entity, CallbackInfo info) {
        EnchantedFurnaceBlock.Companion.accelerateCookTime(entity);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;setLastRecipe(Lnet/minecraft/recipe/Recipe;)V"))
    private static void increaseOutputAmount(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity entity, CallbackInfo info) {
        EnchantedFurnaceBlock.Companion.increaseOutputAmount(entity, world);
    }
}