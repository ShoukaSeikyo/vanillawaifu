package net.orandja.vw.kmixin

import net.minecraft.server.MinecraftServer
import net.minecraft.server.WorldGenerationProgressListener
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.profiler.Profiler
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.MutableWorldProperties
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.Spawner
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.ServerWorldProperties
import net.minecraft.world.level.storage.LevelStorage
import net.orandja.vw.mods.ExtraSaveData
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.concurrent.Executor
import java.util.function.Supplier

@Mixin(ServerWorld::class)
abstract class ServerWorldMixin(properties: MutableWorldProperties?, registryRef: RegistryKey<World>?, dimensionType: DimensionType?, profiler: Supplier<Profiler>?, isClient: Boolean,
                                debugWorld: Boolean, seed: Long
) : World(properties, registryRef, dimensionType,
    profiler,
    isClient, debugWorld, seed
), StructureWorldAccess {

    @Inject(
        at = [At("RETURN")],
        method = ["<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage\$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/gen/chunk/ChunkGenerator;ZJLjava/util/List;Z)V"]
    )
    open fun init(
        server: MinecraftServer?,
        workerExecutor: Executor?,
        session: LevelStorage.Session,
        properties: ServerWorldProperties?,
        registryKey: RegistryKey<World?>?,
        dimensionType: DimensionType?,
        worldGenerationProgressListener: WorldGenerationProgressListener?,
        chunkGenerator: ChunkGenerator?,
        debugWorld: Boolean,
        l: Long,
        list: List<Spawner?>?,
        bl: Boolean,
        info: CallbackInfo?
    ) {
        if(registryKey!! == World.OVERWORLD) {
            ExtraSaveData.load()
        }
    }

    @Inject(method = ["saveLevel"], at = [At("RETURN")])
    fun saveLevel(info: CallbackInfo) {
        if(registryKey == World.OVERWORLD) {
            ExtraSaveData.save()
        }
    }

}