package net.orandja.vw

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.orandja.vw.logic.*
import net.orandja.vw.mods.AnimalTemptation.AnimalTemptation
import net.orandja.vw.mods.WhitelistedChestBlock.WhitelistedChestBlock
import java.util.concurrent.atomic.AtomicReference

class VW : ModInitializer {

    companion object {
        var serverReference: AtomicReference<MinecraftServer> = AtomicReference(null);
        val server: MinecraftServer
            get() {
                return serverReference.get()
            }
    }


    override fun onInitialize() {
        AnimalTemptation.beforeLaunch()

        CloudShulkerBox.beforeLaunch()
        DeepBarrelBlock.beforeLaunch()
        DoubleTool.beforeLaunch()
        EnchantedHopper.beforeLaunch()
        EnchantedBrewingStand.beforeLaunch()
        EnchantedFurnaceBlock.beforeLaunch()
        EnchantedHopper.beforeLaunch()
        InfinityBucket.beforeLaunch()
        ShoppingBarrel.beforeLaunch()

        WhitelistedChestBlock.beforeLaunch()

        ServerLifecycleEvents.SERVER_STARTED.register(serverReference::set)
    }
}