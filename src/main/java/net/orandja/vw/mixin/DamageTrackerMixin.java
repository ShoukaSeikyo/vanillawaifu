package net.orandja.vw.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.math.BlockPos;
import net.orandja.vw.logic.DeathMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.AttributeSet;
import java.util.List;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin implements DeathMessage {

    @Final @Shadow @Getter private LivingEntity entity;

    @Inject(at = @At("RETURN"), method = "getDeathMessage", cancellable = true)
    public void getDeathMessage(CallbackInfoReturnable<Text> info) {
        sendDeathPosition(info);
    }
}
