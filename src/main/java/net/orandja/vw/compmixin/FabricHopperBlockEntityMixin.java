package net.orandja.vw.compmixin;


import net.minecraft.block.entity.Hopper;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.logic.EnchantedHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.mixin.transfer.HopperBlockEntityMixin;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.mixin.transfer.HopperBlockEntityMixin", remap = false)
public abstract class FabricHopperBlockEntityMixin implements EnchantedHopper {

    @SuppressWarnings("ALL")
    @Redirect(method = "hookExtract", at = @At(value = "NEW", target = "Lnet/minecraft/util/math/BlockPos;"))
    private static BlockPos offsetHook(double d, double e, double f, World world, Hopper hopper, CallbackInfoReturnable<Boolean> cir, Inventory inputInventory) {
        System.out.println(e + " -> " + hopper.getHopperY() + Companion.hopperOffset(hopper) + 1.0);
        return new BlockPos(hopper.getHopperX(), hopper.getHopperY() + Companion.hopperOffset(hopper) + 1.0, hopper.getHopperZ());
    }

    @Redirect(method = "hookExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/Hopper;getHopperY()D"))
    private static double getOffsetInventory(Hopper hopper) {
        System.out.println(hopper.getHopperY() + ", " + Companion.hopperOffset(hopper));
        return hopper.getHopperY() + Companion.hopperOffset(hopper);
    }

}