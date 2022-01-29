@file:Mixin(ScreenHandler::class)
@file:JvmName("ScreenHandlerAccessor")

package net.orandja.vw.accessors

import net.minecraft.screen.ScreenHandler
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ScreenHandler::class)
interface ScreenHandlerAccessor {
    @Accessor
    fun getListeners(): List<net.minecraft.screen.ScreenHandlerListener>
}