@file:JvmName("RecipeManagerAccessor")
@file:Mixin(CraftingScreenHandler::class)

package net.orandja.vw.accessors

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.CraftingScreenHandler
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(CraftingScreenHandler::class)
interface CraftingScreenHandlerAccessor {

    @Accessor
    fun getPlayer(): PlayerEntity
}