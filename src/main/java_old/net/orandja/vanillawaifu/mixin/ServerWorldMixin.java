package net.orandja.vanillawaifu.mixin;

import com.google.common.collect.Maps;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.orandja.vanillawaifu.VanillaWaifu;
import net.orandja.vanillawaifu.utils.QuickUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess {

    protected ServerWorldMixin() {
        super(null, null, null, null, false, false, 0);
    }



    File file;
    File file2;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/gen/chunk/ChunkGenerator;ZJLjava/util/List;Z)V")
    public void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<Spawner> list, boolean bl, CallbackInfo info) {
        file = session.getWorldDirectory(this.getRegistryKey());
        file2 = new File(file, "vanillawaifu.dat");

        if(file2.exists()) {

        }
    }

    @Inject(method = "saveLevel", at = @At("RETURN"))
    private void saveLevel(CallbackInfo info) {
        CompoundTag mainTag = new CompoundTag();
        VanillaWaifu.WORLD_DATA.computeIfAbsent(this.getRegistryKey().getValue(), _ignored -> Maps.newHashMap()).forEach((namespace, consumer) -> mainTag.put(namespace, QuickUtils.create(new CompoundTag(), consumer)));
    }

    public CompoundTag readTag(String id, int dataVersion) throws IOException {
        File file = file2;
        FileInputStream fileInputStream = new FileInputStream(file);
        Throwable var5 = null;

        Object var10;
        try {
            PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);
            Throwable var7 = null;

            try {
                CompoundTag compoundTag3;
                DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);
                var10 = null;

                try {
                    compoundTag3 = NbtIo.read(dataInputStream);
                } catch (Throwable var54) {
                    var10 = var54;
                    throw var54;
                } finally {
                    if (dataInputStream != null) {
                        if (var10 != null) {
                            try {
                                dataInputStream.close();
                            } catch (Throwable var53) {
                                ((Throwable)var10).addSuppressed(var53);
                            }
                        } else {
                            dataInputStream.close();
                        }
                    }

                }

            } catch (Throwable var56) {
                var7 = var56;
                throw var56;
            } finally {
                if (pushbackInputStream != null) {
                    if (var7 != null) {
                        try {
                            pushbackInputStream.close();
                        } catch (Throwable var52) {
                            var7.addSuppressed(var52);
                        }
                    } else {
                        pushbackInputStream.close();
                    }
                }

            }
        } catch (Throwable var58) {
            var5 = var58;
            throw var58;
        } finally {
            if (fileInputStream != null) {
                if (var5 != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable var51) {
                        var5.addSuppressed(var51);
                    }
                } else {
                    fileInputStream.close();
                }
            }

        }

        return (CompoundTag)var10;
    }
}
