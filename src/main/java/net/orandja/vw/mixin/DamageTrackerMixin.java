package net.orandja.vw.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.AttributeSet;
import java.util.List;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {

    @Shadow
    private LivingEntity entity;

    @Inject(at = @At("RETURN"), method = "getDeathMessage", cancellable = true)
    public void getDeathMessage(CallbackInfoReturnable<Text> info) {
        if(info.getReturnValue() instanceof TranslatableText text) {
            BlockPos pos = entity.getBlockPos();
            Text.of(" [" + pos.getX() + "; " + pos.getY() + "; " + pos.getZ() + "] in " + entity.getEntityWorld().getRegistryKey().getValue().toString()).getWithStyle(Style.EMPTY.withColor(TextColor.parse("green"))).forEach(text::append);
        }
//        info.setReturnValue();
    }
}
