package net.orandja.vw.mods

import net.minecraft.entity.ai.goal.GoalSelector
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.*
import net.minecraft.tag.ItemTags
import net.minecraft.world.World
import net.orandja.vw.ai.goal.TemptItemGoal
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private fun GoalSelector.add(pair: Pair<Int, TemptItemGoal>?) {
    if(pair != null)
        this.add(pair.first, pair.second)
}

class AnimalTemptation {

    companion object {

        private fun getTemptation(entity: Any): ((AnimalEntity) -> Pair<Int, TemptItemGoal>)? {
            val animal = entity as? AnimalEntity ?: return null
            val key = TEMPTATIONS.keys.firstOrNull { animal::class.isSubclassOf(it) }

            return TEMPTATIONS[key]
        }

        private val TEMPTATIONS: HashMap<KClass<out AnimalEntity>, (AnimalEntity) -> Pair<Int, TemptItemGoal>> = HashMap()

        fun beforeLaunch() {
            TEMPTATIONS[AxolotlEntity::class] = { Pair(3, TemptItemGoal(it, 0.66, (it as AxolotlEntity)::isBreedingItem, 0)) }
            TEMPTATIONS[BeeEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as BeeEntity)::isBreedingItem, 0)) }
            TEMPTATIONS[CatEntity::class] = { Pair(10, TemptItemGoal(it, 1.25, (it as CatEntity)::isBreedingItem)) }
            TEMPTATIONS[ChickenEntity::class] = { Pair(10, TemptItemGoal(it, 1.25, (it as ChickenEntity)::isBreedingItem)) }
            TEMPTATIONS[CowEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as CowEntity)::isBreedingItem)) }
            TEMPTATIONS[FoxEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as FoxEntity)::isBreedingItem)) }
            TEMPTATIONS[HorseBaseEntity::class] = { Pair(4, TemptItemGoal(it, 1.25, (it as HorseBaseEntity)::isBreedingItem)) }
            TEMPTATIONS[LlamaEntity::class] = { Pair(4, TemptItemGoal(it, 1.25, (it as LlamaEntity)::isBreedingItem)) }
            TEMPTATIONS[OcelotEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as OcelotEntity)::isBreedingItem)) }
            TEMPTATIONS[PigEntity::class] = { Pair(4, TemptItemGoal(it, 1.25, (it as PigEntity)::isBreedingItem)) }
            TEMPTATIONS[RabbitEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as RabbitEntity)::isBreedingItem)) }
            TEMPTATIONS[SheepEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as SheepEntity)::isBreedingItem)) }
            TEMPTATIONS[WolfEntity::class] = { Pair(9, TemptItemGoal(it, 1.25, (it as WolfEntity)::isBreedingItem)) }
            TEMPTATIONS[TurtleEntity::class] = { Pair(2, TemptItemGoal(it, 1.25, (it as TurtleEntity)::isBreedingItem)) }
        }

        fun applyTo(world: World?, entity: Any, goalSelector: GoalSelector) {
            if(world?.isClient == false) {
                val animal = entity as? AnimalEntity ?: return
                goalSelector.add(getTemptation(animal)?.invoke(animal))
            }
        }
    }

}