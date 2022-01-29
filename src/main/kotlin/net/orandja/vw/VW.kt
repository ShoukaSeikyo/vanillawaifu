package net.orandja.vw

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.orandja.vw.logic2.WhitelistedChestBlock
import net.orandja.vw.mods.*
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
        CloudBox.beforeLaunch()

        ServerLifecycleEvents.SERVER_STARTED.register { mcserv ->
            serverReference.set(mcserv)

            WhitelistedChestBlock.launch()
            EnchantMore.launch()
            CloudBox.launch()
            DoubleTools.launch()
        }
    }
}