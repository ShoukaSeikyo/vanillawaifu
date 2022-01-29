package net.orandja.vw.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.FurnaceEntityLogic;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin_old extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, FurnaceEntityLogic {
    protected AbstractFurnaceBlockEntityMixin_old(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    short smite = 0;
    public short getSmite() {
        return smite;
    }
    public void setSmite(short level) {
        smite = level;
    }

    short efficiency = 0;
    public short getEfficiency() {
        return efficiency;
    }
    public void setEfficiency(short level) {
        efficiency = level;
    }

    short flame = 0;
    public short getFlame() {
        return flame;
    }
    public void setFlame(short level) {
        flame = level;
    }

    short unbreaking = 0;
    public short getUnbreaking() {
        return unbreaking;
    }
    public void setUnbreaking(short level) {
        unbreaking = level;
    }

    short fireAspect = 0;
    public short getFireAspect() {
        return fireAspect;
    }
    public void setFireAspect(short level) {
        fireAspect = level;
    }

    short fortune = 0;
    public short getFortune() {
        return fortune;
    }
    public void setFortune(short level) {
        fortune = level;
    }

    @Shadow
    int burnTime;
    public int getBurnTime() {
        return burnTime;
    }
    public void setBurnTime(int value) {
        burnTime = value;
    }

    @Shadow
    int fuelTime;
    public int getFuelTime() {
        return fuelTime;
    }
    public void setFuelTime(int value) {
        fuelTime = value;
    }

    @Shadow
    // Accelerate to 200 ticks
    int cookTime;
    public int getCookTime() {
        return cookTime;
    }
    public void setCookTime(int value) {
        cookTime = value;
    }

    @Shadow
    // Should always be 200 ticks
    int cookTimeTotal;
    public int getCookTimeTotal() {
        return cookTimeTotal;
    }
    public void setCookTimeTotal(int value) {
        cookTimeTotal = value;
    }

    @Shadow
    DefaultedList<ItemStack> inventory;
    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    @Shadow
    abstract boolean isBurning();
    public boolean burning() {
        return isBurning();
    }

    /*
        EFFICIENCY, SMITE -> faster processing, combustible burns faster
        FLAME -> keeps flame from being used while idle.
        UNBREAKING, FIRE_ASPECT -> combustible burns longer
        FORTUNE -> outputs more items on cooking.
     */

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound tag, CallbackInfo info) {
        this.fromTag(tag, info);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void writeNbt(NbtCompound tag, CallbackInfoReturnable<NbtCompound> info) {
        this.toTag(tag, info);
    }

    @Redirect( method ="tick", at = @At( value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0,
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I"
    ))
    private static void decreaseBurnTime(AbstractFurnaceBlockEntity entity, int ignored) {
        FurnaceEntityLogic.Companion.decreaseBurnTime(entity);
//        castAs(AbstractFurnaceBlockEntityMixin.class, entity, (furnace) -> {
//            if (furnace.isBurning() && (furnace.flame == 0 || !furnace.inventory.get(0).isEmpty())) {
//                furnace.burnTime = Math.max(0, furnace.burnTime - Math.max(1, (furnace.efficiency + furnace.smite) * 2));
//            }
//        });
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 1,
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I"
    ))
    private static void setBurnTime(AbstractFurnaceBlockEntity entity, int burnTime) {
        FurnaceEntityLogic.Companion.setBurnTime(entity, burnTime);
//        castAs(AbstractFurnaceBlockEntityMixin.class, entity, (furnace) -> {
//            furnace.burnTime = burnTime + ((burnTime * (furnace.unbreaking + furnace.fireAspect)) / 5);
//        });
    }


    @Inject(method = "tick", at = @At(value = "FIELD", shift = At.Shift.AFTER, ordinal = 1,
            target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;cookTime:I"
    ))
    private static void accelerateCookTime(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity entity, CallbackInfo info) {
        FurnaceEntityLogic.Companion.accelerateCookTime(entity);
        System.out.println(((FurnaceEntityLogic)entity).getEfficiency());
//        castAs(AbstractFurnaceBlockEntityMixin.class, entity, (furnace) -> {
//            if (furnace.efficiency + furnace.smite > 0)
//                furnace.cookTime = MathHelper.clamp(furnace.cookTime - 1 + Math.max(1, (furnace.efficiency + furnace.smite) * 2), 0, furnace.cookTimeTotal);
//        });
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;setLastRecipe(Lnet/minecraft/recipe/Recipe;)V"
    ))
    private static void increaseOutputAmount(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity entity, CallbackInfo info) {
        FurnaceEntityLogic.Companion.increaseOutputAmount(entity, world);
//        castAs(AbstractFurnaceBlockEntityMixin.class, entity, (furnace) -> {
//            if (furnace.fortune < 1) return;
//            furnace.inventory.get(2).increment(Math.max(0, world.random.nextInt(furnace.fortune + 2) - 1));
//        });
    }
}