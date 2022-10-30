package net.orandja.vw.mods.FasterMobSpawner;

import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.orandja.mcutils.MathUtils;

public interface FasterMobSpawner {

    int getSpawnDelay();
    int setSpawnDelay(int delay);

    int getRequiredPlayerRange();

    int getDelayReduction();
    void setDelayReduction(int delay);

    int getNextLoad();
    void setNextLoad(int load);

    default void tickBetterSpawner(MobSpawnerLogic logic, ServerWorld world, BlockPos pos) {
        double requiredDistance = (getRequiredPlayerRange() * getRequiredPlayerRange());
        if(getSpawnDelay() <= getNextLoad()) {
            // 100⌊x/100⌋ to eliminate double digits ticks... I don't remember why; should have commented earlier. LUL
            setNextLoad((int) (Math.floor(getSpawnDelay() / 100.0D) * 100));
            long playerCount = world.getPlayers(EntityPredicates.EXCEPT_SPECTATOR).stream().filter(EntityPredicates.VALID_LIVING_ENTITY).filter(MathUtils.inRange(pos, requiredDistance)).count();
            setDelayReduction((int) Math.max(1, Math.pow(3, (playerCount - 1))));
        }

        if((setSpawnDelay(Math.max(0, getSpawnDelay() - getDelayReduction()))) <= 0) {
            setNextLoad(800);
        }
    }
}
