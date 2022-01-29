package net.orandja.vw.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.orandja.vw.utils.ItemUtilsKt.toArray;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "onPlayerCollision", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, target = "Lnet/minecraft/entity/player/PlayerEntity;experiencePickUpDelay:I"))
    public void onPlayerCollision(PlayerEntity player, int ignored) {
        player.experiencePickUpDelay = 1;
    }

    @Shadow
    private native int getMendingRepairAmount(int experienceAmount);

    @Shadow
    private native int getMendingRepairCost(int repairAmount);

    @Shadow
    private native int repairPlayerGears(PlayerEntity player, int amount);

    @Shadow
    private int amount;

    @Inject(method = "repairPlayerGears", at = @At("HEAD"), cancellable = true)
    private void repairPlayerGears(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> info) {
//        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.MENDING, player, ItemStack::isDamaged);
        Optional<ItemStack> mendingStack = Arrays.stream(toArray(player.getInventory(), false)).filter(ItemStack::isDamaged).filter(stack -> EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0).findFirst();
        if (mendingStack.isPresent()) {
            ItemStack itemStack = mendingStack.get();
            int i = Math.min(this.getMendingRepairAmount(this.amount), itemStack.getDamage());
            itemStack.setDamage(itemStack.getDamage() - i);
            int j = amount - this.getMendingRepairCost(i);
            info.setReturnValue(j > 0 ? this.repairPlayerGears(player, j) : 0);
        }
    }

}
