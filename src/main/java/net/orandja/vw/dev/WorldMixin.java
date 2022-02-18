package net.orandja.vw.dev;

import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin
        implements WorldAccess,
        AutoCloseable {

    private int radius = 30;
    private int diameter = (radius * 2) + 1;
    private int max = radius;
    private int min = max * -1;

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
    public void getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> info) {
        System.out.println(chunkX + " -> " + loopAround(chunkX) + "; " + chunkZ + " -> " + loopAround(chunkZ));
        boolean shouldCreate = chunkX > radius || chunkX < -radius || chunkZ > radius || chunkZ < -radius;
        Chunk chunk = this.getChunkManager().getChunk(loopAround(chunkX), loopAround(chunkZ), leastStatus, !shouldCreate);
        if (chunk == null && !shouldCreate) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        info.setReturnValue(chunk);
    }

    public int loopAround(int coordinate) {
        if(coordinate < min) {
            coordinate = (coordinate % diameter) + radius;
        } else if(coordinate > radius) {
            coordinate = (coordinate % diameter);
            if(coordinate > radius)
                coordinate -= diameter;
        }
        return coordinate;
    }

}