package net.orandja.vw.logic

import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

interface NerfedEntity {

    var tickCooldown: Short
    var mobDead: Boolean

    @Inject(at = [At("HEAD")], method = ["tick"], cancellable = true)
    fun nerf(callbackInfo: CallbackInfo) {
        tickCooldown--
        if (tickCooldown > 0 && !this.mobDead) {
            callbackInfo.cancel()
            return
        }
        tickCooldown = 30
    }

}