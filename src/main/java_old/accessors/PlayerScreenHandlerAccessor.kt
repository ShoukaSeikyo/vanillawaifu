@file:JvmName("PlayerScreenHandlerAccessor")
@file:Mixin(PlayerScreenHandler::class)

package net.orandja.vw.accessors

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.CraftingScreenHandler
import net.minecraft.screen.PlayerScreenHandler
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(PlayerScreenHandler::class)
interface PlayerScreenHandlerAccessor {

    @Accessor
    fun getOwner(): PlayerEntity
}