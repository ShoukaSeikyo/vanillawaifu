@file:Suppress("unused")

package net.orandja.vw

import net.minecraft.SharedConstants
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.io.*

@SuppressWarnings("unused")
interface ExtraSaveData {
    companion object {

        val CLEAR: HashMap<String, () -> Unit> = HashMap()
        val LOAD: HashMap<String, (NbtCompound) -> Unit> = HashMap()
        val SAVE: HashMap<String, () -> NbtCompound> = HashMap()

        private val file = File("config/vw-extradata.dat")
        var loadedCompound: NbtCompound? = null

        private fun readNbt() {
            if (File("config/vw-extradata.dat").exists()) {
                var pushbackInputStream: PushbackInputStream? = null
                try {
                    pushbackInputStream = PushbackInputStream(FileInputStream(file), 2)
                    val dataInputStream = DataInputStream(pushbackInputStream)
                    loadedCompound = NbtIo.readCompressed(pushbackInputStream)
                    dataInputStream.close()
                    pushbackInputStream.close()
                } catch (e: Exception) {
                    pushbackInputStream?.close()
                    e.printStackTrace()
                    loadedCompound = NbtCompound()
                }
            } else {
                loadedCompound = NbtCompound()
            }
        }

        fun onLoad(name: String, consumer: (NbtCompound) -> Unit) {
            LOAD[name] = consumer
            if (loadedCompound != null) {
                val dataCompound = loadedCompound!!.getCompound("data")
                if (dataCompound.contains(name))
                    consumer(dataCompound.getCompound(name))
            }
        }

        fun clear() {
            CLEAR.forEach { entry -> entry.value.invoke() }
        }

        fun onClear(name: String, consumer: () -> Unit) {
            CLEAR[name] = consumer
        }

        fun onSave(name: String, consumer: () -> NbtCompound) {
            SAVE[name] = consumer
        }

    }
    fun load(registryKey: RegistryKey<World>) {
        if (registryKey !== World.OVERWORLD) {
            return
        }

        if (loadedCompound == null) {
            readNbt()
        }
        val dataCompound = loadedCompound!!.getCompound("data")
        LOAD.forEach {
            if (dataCompound.contains(it.key))
                it.value(dataCompound.getCompound(it.key))
        }
    }

    fun save(registryKey: RegistryKey<World>) {
        if (registryKey !== World.OVERWORLD) {
            return
        }
        val mainCompound = NbtCompound()
        mainCompound.put("data", NbtCompound().apply {
            SAVE.forEach { put(it.key, it.value()) }
        })
        @Suppress("DEPRECATION")
        mainCompound.putInt("DataVersion", SharedConstants.getGameVersion().worldVersion)

        try {
            NbtIo.writeCompressed(mainCompound, file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        loadedCompound = mainCompound
    }
}