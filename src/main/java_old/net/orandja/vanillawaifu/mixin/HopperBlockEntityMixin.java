package net.orandja.vanillawaifu.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.orandja.vanillawaifu.mods.ProtectBlock;
import net.orandja.vanillawaifu.utils.BlockUtils;
import net.orandja.vanillawaifu.utils.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity implements Hopper, Tickable {
    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Shadow
    abstract void setCooldown(int cooldown);

    @Shadow
    private int transferCooldown;

    @Shadow
    abstract Inventory getOutputInventory();

    private short mending;
    private short silkTouch;
    private short efficiency;

    @Inject(at = @At("RETURN"), method = "<init>()V")
    private void init(CallbackInfo info) {
        BlockUtils.<CompoundTag>observe(this, "Enchantments", (tag) -> {
            short level = tag.getShort("lvl");
            switch (tag.getString("id").replace("minecraft:", "")) {
                case "silk_touch":
                    silkTouch = level;
                    break;
                case "efficiency":
                    efficiency = level;
                    break;
                case "mending":
                    mending = level;
                    break;
            }
        });
        BlockUtils.accessor(this, "enchanted", () -> this.isEnchanted());
        BlockUtils.accessor(this, "Enchantments", () -> this.getEnchantments());
    }

    @Inject(method = "extract(Lnet/minecraft/block/entity/Hopper;)Z", at = @At("HEAD"), cancellable = true)
    private static void extract(Hopper hopper, CallbackInfoReturnable<Boolean> info) {
        if (ProtectBlock.preventExtract(hopper.getWorld(), hopper.getHopperX(), hopper.getHopperY() + 1D, hopper.getHopperZ()))
            info.setReturnValue(false);
    }

    public boolean isEnchanted() {
        return (silkTouch > 0 || efficiency > 0 || mending > 0);
    }

    public Map<String, Short> getEnchantments() {
        return new HashMap<String, Short>() {{
            put("efficiency", efficiency);
            put("silk_touch", silkTouch);
            put("mending", mending);
        }};
    }

    @Inject(method = "insertAndExtract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;setCooldown(I)V", shift = At.Shift.AFTER)
    )
    public void setCooldown(CallbackInfoReturnable<Boolean> info) {
        this.setCooldown(Math.max(1, this.isEnchanted() ? transferCooldown - (int) ((((double) transferCooldown - 2) / 5D) * ((double) efficiency)) : transferCooldown));
    }

    @Redirect(method = "insert", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
    public boolean filter(ItemStack stack) {
        return stack.isEmpty() || (silkTouch == 1 && stack.getCount() == 1);
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

    @Redirect(method = "insertAndExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
    public Comparable redstoneEnabled(BlockState state, Property value) {
        return (this.mending == 1 && !this.getCachedState().get(HopperBlock.ENABLED)) || this.getCachedState().get(HopperBlock.ENABLED);
    }

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void insert(CallbackInfoReturnable<Boolean> info) {
        if (this.mending == 0) {
            return;
        }

        Inventory toInv = this.getOutputInventory();
        if (toInv == null) {
            info.setReturnValue(false);
            return;
        }

        int power = this.world.getReceivedRedstonePower(this.pos);
        if (power >= toInv.size()) {
            info.setReturnValue(false);
            return;
        }

        for (int i = 0; i < this.size(); ++i) {
            ItemStack stackFromInv = this.getStack(i);
            if (!stackFromInv.isEmpty()) {
                ItemStack stackToInv = toInv.getStack(power);
                if(stackToInv.isEmpty()) {
                    toInv.setStack(power, Inventories.splitStack(this.getInvStackList(), i, 1));
                    info.setReturnValue(true);
                    return;
                } else if(stackToInv.isEmpty() || ItemUtils.canMergeItems(stackToInv, stackFromInv)) {
                    stackFromInv.decrement(1);
                    stackToInv.increment(1);

                    this.setStack(i, stackFromInv);
                    toInv.setStack(power, stackToInv);
                    info.setReturnValue(true);
                    return;
                }
            }
        }

        info.setReturnValue(false);
        return;
    }

    public boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || (a.isItemEqual(b) && ItemStack.areTagsEqual(a, b));
    }
}
