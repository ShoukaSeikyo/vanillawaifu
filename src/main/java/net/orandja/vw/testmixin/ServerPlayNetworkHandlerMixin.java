package net.orandja.vw.testmixin;

import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onPickFromInventory", at = @At("HEAD"))
    public void onPickFromInventory(PickFromInventoryC2SPacket packet, CallbackInfo ci) {
    }
}