package net.orandja.vw

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.orandja.vw.logic.*
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
        WhitelistedChestBlock.beforeLaunch()

        ServerLifecycleEvents.SERVER_STARTED.register(serverReference::set)
    }
}