//package net.orandja.vw.logic
//
//import net.minecraft.predicate.entity.EntityPredicates
//import net.minecraft.server.world.ServerWorld
//import net.minecraft.util.math.BlockPos
//import net.minecraft.world.MobSpawnerLogic
//import kotlin.math.floor
//import kotlin.math.max
//import kotlin.math.pow
//
//interface MobSpawnerMixinLogic {
//
//    var spawnDelay: Int
//    val requiredPlayerRange: Int
//    var delayReduction: Int
//    var nextLoad: Int
//
//    fun serverTick(logic: MobSpawnerLogic, world: ServerWorld, pos: BlockPos) {
//        val d = (requiredPlayerRange * requiredPlayerRange).toDouble()
//        if (spawnDelay <= nextLoad) {
//            nextLoad = (floor(spawnDelay / 100.0) * 100).toInt()
//            val playerCount = world.getPlayers(EntityPredicates.EXCEPT_SPECTATOR::test).filter(EntityPredicates.VALID_LIVING_ENTITY::test).count {
//                it.squaredDistanceTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) < d
//            }
//            delayReduction = max(1, 3.0.pow(playerCount - 1).toInt())
//        }
//
//        if (max(0, spawnDelay - delayReduction).also { spawnDelay = it } <= 0) {
//            nextLoad = 800
//        }
//    }
//
//}