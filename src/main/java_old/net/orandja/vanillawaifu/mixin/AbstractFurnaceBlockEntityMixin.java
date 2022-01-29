package net.orandja.vanillawaifu.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.utils.BlockUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider, Tickable {


    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    /*
        EFFICIENCY, SMITE -> faster processing, combustible burns faster
        FLAME -> keeps flame from being used while idle.
        UNBREAKING, FIRE_ASPECT -> combustible burns longer
        FORTUNE -> outputs more items on cooking.
     */

    private short smite;
    private short efficiency;
    private short flame;
    private short unbreaking;
    private short fireAspect;
    private short fortune;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/block/entity/BlockEntityType;Lnet/minecraft/recipe/RecipeType;)V")
    private void init(BlockEntityType blockEntityType, RecipeType recipeType, CallbackInfo info) {
        BlockUtils.<CompoundTag>observe(this, "Enchantments", (tag) -> {
            short level = tag.getShort("lvl");
            switch (tag.getString("id").replace("minecraft:", "")) {
                case "smite":
                    smite = level;
                    break;
                case "efficiency":
                    efficiency = level;
                    break;
                case "unbreaking":
                    unbreaking = level;
                    break;
                case "flame":
                    flame = level;
                    break;
                case "fire_aspect":
                    fireAspect = level;
                    break;
                case "fortune":
                    fortune = level;
                    break;
            }
        });
        BlockUtils.accessor(this, "enchanted", () -> this.isEnchanted());
        BlockUtils.accessor(this, "Enchantments", () -> this.getEnchantments());
    }

    public boolean isEnchanted() {
        return (smite > 0 || efficiency > 0 || flame > 0 || unbreaking > 0 || fireAspect > 0 || fortune > 0);
    }

    public Map<String, Short> getEnchantments() {
        return new HashMap<String, Short>() {{
            put("efficiency", efficiency);
            put("smite", smite);
            put("flame", flame);
            put("unbreaking", unbreaking);
            put("fire_aspect", fireAspect);
            put("fortune", fortune);
        }};
    }

    @Shadow
    private int burnTime;
    @Shadow
    private int fuelTime;
    @Shadow
    private int cookTime; // Accelerate to 200 ticks
    @Shadow
    private int cookTimeTotal; // Should always be 200 ticks
    @Shadow
    protected DefaultedList<ItemStack> inventory;

    @Shadow
    abstract boolean isBurning();

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    public void decreaseBurnTime(AbstractFurnaceBlockEntity blockEntity, int ignored) {
        if (this.isBurning()) {
            if (this.flame > 0 && this.inventory.get(0).isEmpty()) {
                return;
            }

            burnTime = Math.max(0, burnTime - 1);
        }
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I", opcode = Opcodes.PUTFIELD, ordinal = 1))
    public void setBurnTime(AbstractFurnaceBlockEntity blockEntity, int burnTime) {
        this.burnTime = burnTime + ((burnTime * (unbreaking + fireAspect)) / 5);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;cookTime:I", shift = At.Shift.AFTER, ordinal = 1))
    public void accelerateCookTime(CallbackInfo info) {
        if ((efficiency + smite) > 0)
            cookTime = MathHelper.clamp(cookTime - 1 + Math.max(1, ((efficiency + smite) * 2)), 0, cookTimeTotal);
    }

    @Redirect(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"))
    private void increaseOutputAmount(ItemStack itemstack, int amount) {
        itemstack.increment(this.fortune < 1 ? amount : amount * (Math.max(0, this.world.random.nextInt(this.fortune + 2) - 1) + 1));
    }

    @Inject(method = "fromTag", at = @At("HEAD"))
    public void fromTag(BlockState state, CompoundTag tag, CallbackInfo info) {
        if (!tag.contains("Enchantments"))
            return;

        ((ListTag) tag.get("Enchantments")).forEach(enchantment -> BlockUtils.notify(this, "Enchantments", enchantment));
    }

    @Inject(method = "toTag", at = @At("HEAD"))
    public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        if (!this.isEnchanted()) {
            return;
        }

        ListTag listTag = new ListTag();
        this.getEnchantments().forEach((name, level) -> {
            if (level > 0) {
                CompoundTag enchantment = new CompoundTag();
                enchantment.putShort("lvl", level);
                enchantment.putString("id", "minecraft:" + name);
                listTag.add(enchantment);
            }
        });
        tag.put("Enchantments", listTag);
    }
}
