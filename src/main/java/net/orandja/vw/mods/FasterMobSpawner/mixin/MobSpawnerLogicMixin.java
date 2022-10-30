package net.orandja.vw.mods.FasterMobSpawner.mixin;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.orandja.vw.mods.FasterMobSpawner.FasterMobSpawner;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin implements FasterMobSpawner {

    @Shadow @Getter private int spawnDelay;
    public int setSpawnDelay(int delay) {
        this.spawnDelay = delay;
        return this.spawnDelay;
    }
    @Shadow @Getter @Setter private int requiredPlayerRange;

    @Getter @Setter int delayReduction = 1;
    @Getter @Setter int nextLoad = 800;

    @Redirect(method = "serverTick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 0, target = "Lnet/minecraft/world/MobSpawnerLogic;spawnDelay:I"))
    private void serverTick(MobSpawnerLogic logic, int ignored, ServerWorld world, BlockPos pos) {
        tickBetterSpawner(logic, world, pos);
    }
}
