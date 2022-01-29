package net.orandja.vanillawaifu.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;
import net.orandja.vanillawaifu.utils.BlockUtils;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity implements SidedInventory, Tickable {

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    private short baneOfArthropods;
    private short efficiency;
    private short unbreaking;
    private short fireAspect;
    private short silkTouch;

    @Inject(at = @At("RETURN"), method = "<init>()V")
    private void init(CallbackInfo info) {
        BlockUtils.<CompoundTag>observe(this, "Enchantments", (tag) -> {
            short level = tag.getShort("lvl");
            switch (tag.getString("id").replace("minecraft:", "")) {
                case "bane_of_arthropods":
                    baneOfArthropods = level;
                    break;
                case "efficiency":
                    efficiency = level;
                    break;
                case "unbreaking":
                    unbreaking = level;
                    break;
                case "fire_aspect":
                    fireAspect = level;
                    break;
                case "silk_touch":
                    silkTouch = level;
                    break;
            }
        });
        BlockUtils.accessor(this, "enchanted", () -> this.isEnchanted());
        BlockUtils.accessor(this, "protected", () -> silkTouch == 1 && brewTime > 0);
        BlockUtils.accessor(this, "Enchantments", () -> this.getEnchantments());
    }

    public boolean isEnchanted() {
        return (baneOfArthropods > 0 || efficiency > 0 || unbreaking > 0 || fireAspect > 0);
    }

    public Map<String, Short> getEnchantments() {
        return new HashMap<String, Short>() {{
            put("efficiency", efficiency);
            put("bane_of_arthropods", baneOfArthropods);
            put("unbreaking", unbreaking);
            put("fire_aspect", fireAspect);
            put("silk_touch", silkTouch);
        }};
    }

    @Shadow
    private int brewTime;
    @Shadow
    private int fuel;

    @Redirect(method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;fuel:I", opcode = Opcodes.PUTFIELD, ordinal = 0)
    )
    public void setFuelCount(BrewingStandBlockEntity blockEntity, int fuel) {
        this.fuel = fuel + ((fuel * (unbreaking + fireAspect)) / 5);
    }

    @Inject(method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;brewTime:I", shift = At.Shift.AFTER, ordinal = 0)
    )
    public void accelerate(CallbackInfo info) {
        if ((efficiency + baneOfArthropods) > 0)
            this.brewTime = MathHelper.clamp(this.brewTime + 1 - Math.max(1, ((efficiency + baneOfArthropods) * 2)), 0, 400);
    }
}
