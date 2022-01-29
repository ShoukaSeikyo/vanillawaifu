package net.orandja.vw.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

//    @Inject(at = @At("HEAD"), cancellable = true, method = "chooseEquipmentWith")
//    private static void chooseEquipmentWith(Enchantment enchantment, LivingEntity entity, Predicate<ItemStack> condition, CallbackInfoReturnable<Map.Entry<EquipmentSlot, ItemStack>> info) {
////        List.get
//    }

    @Shadow
    private native static int getLevel(Enchantment enchantment, ItemStack stack);

    @Inject(method = "Lnet/minecraft/enchantment/EnchantmentHelper;chooseEquipmentWith(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;",
            at = @At("HEAD"), cancellable = true)
    private static void increaseOutputAmount(Enchantment enchantment, LivingEntity entity, Predicate<ItemStack> condition, CallbackInfoReturnable<Map.Entry<EquipmentSlot, ItemStack>> cir) {
        Map<EquipmentSlot, ItemStack> map = enchantment.getEquipment(entity);
        if (entity instanceof PlayerEntity player) {
            PlayerInventory inventory = player.getInventory();

        }
//        if (map.isEmpty()) {
//            cir.setReturnValue(null);
//            return;
//        } else {
//            List<Map.Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
//            Iterator var5 = map.entrySet().iterator();
//
//            while(var5.hasNext()) {
//                Map.Entry<EquipmentSlot, ItemStack> entry = (Map.Entry)var5.next();
//                ItemStack itemStack = (ItemStack)entry.getValue();
//                if (!itemStack.isEmpty() && getLevel(enchantment, itemStack) > 0 && condition.test(itemStack)) {
//                    if(itemStack.getDamage() != itemStack.getMaxDamage()) {
//                        list.add(entry);
//                    }
//                }
//            }
//
//            cir.setReturnValue(list.isEmpty() ? null : (Map.Entry)list.get(entity.getRandom().nextInt(list.size())));
//        }
    }

}
