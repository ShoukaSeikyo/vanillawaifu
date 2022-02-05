package net.orandja.vw.dev

import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.GoalSelector
import net.minecraft.entity.mob.HoglinEntity
import net.minecraft.entity.passive.*
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private fun GoalSelector.add(pair: Pair<Int, TemptItemGoal>?) {
    if(pair != null)
        this.add(pair.first, pair.second)
}

interface AnimalTemptation {

    companion object {

        private fun getTemptation(animal: AnimalEntity): ((AnimalEntity) -> Pair<Int, TemptItemGoal>)? {
            return TEMPTATIONS[TEMPTATIONS.keys.firstOrNull { animal::class.isSubclassOf(it) || animal::class == it }]
        }

        private val TEMPTATIONS: HashMap<KClass<out AnimalEntity>, (AnimalEntity) -> Pair<Int, TemptItemGoal>> = HashMap()

        fun beforeLaunch() {
            TEMPTATIONS[AxolotlEntity::class] = { Pair(3, TemptItemGoal(it, 0.33, (it as AxolotlEntity)::isBreedingItem, 0.0) { ItemStack(Items.WATER_BUCKET) }) }
            TEMPTATIONS[BeeEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as BeeEntity)::isBreedingItem, 0.0)) }
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

            TEMPTATIONS[StriderEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as StriderEntity)::isBreedingItem)) }
            TEMPTATIONS[GoatEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as GoatEntity)::isBreedingItem)) }
            TEMPTATIONS[HoglinEntity::class] = { Pair(3, TemptItemGoal(it, 1.25, (it as HoglinEntity)::isBreedingItem)) }
        }
    }

    fun applyTo(world: World?, entity: Any, goalSelector: GoalSelector) {
        if(world?.isClient == false) {
            val animal = entity as? AnimalEntity ?: return
            goalSelector.add(getTemptation(animal)?.invoke(animal))
        }
    }
}

class TemptItemGoal(protected val mob: AnimalEntity,
                    private val speed: Double,
                    private var foodPredicate: Predicate<ItemStack>,
                    private val offsetY: Double = 1.0,
                    private var stackHandler: (ItemStack) -> ItemStack = { it.decrement(1); it }
) : Goal() {
    private var itemEntity: ItemEntity? = null
    private var cooldown = 0
    var active = false
        private set

    init {
        controls = EnumSet.of(Control.MOVE, Control.LOOK)
    }

    override fun canStart(): Boolean {
        if (mob.isInLove || mob.isBaby || mob.breedingAge > 0) {
            return false
        }

        if (cooldown > 0) {
            --cooldown
            return false
        }

        itemEntity = mob.entityWorld.getEntitiesByClass(ItemEntity::class.java, mob.boundingBox.expand(16.0, 4.0, 16.0), EntityPredicates.VALID_ENTITY)
            .firstOrNull { entity -> foodPredicate.test(entity.stack) }

        return if(itemEntity == null) {
            cooldown = 100
            false
        } else
            true
    }

    override fun shouldContinue(): Boolean = (itemEntity?.squaredDistanceTo(mob) ?: 36.1) < 36.0 || canStart()

    override fun start() { active = true }

    override fun stop() {
        mob.navigation.stop()
        cooldown = 100
        active = false
        itemEntity = null
    }

    override fun tick() {
        if(itemEntity == null || !itemEntity!!.isAlive) {
            stop()
            return
        }

        mob.lookControl.lookAt(itemEntity, (mob.maxHeadRotation + 20).toFloat(), mob.maxLookPitchChange.toFloat())

        if (mob.squaredDistanceTo(itemEntity) < 1.0 && mob.boundingBox.intersects(itemEntity!!.boundingBox)) {
            if (itemEntity != null) {
                itemEntity!!.stack = stackHandler(itemEntity!!.stack)
                mob.lovePlayer(null)
            }
            stop()
            return
        }

        mob.navigation.startMovingTo(adjust(mob.x, itemEntity!!.x), itemEntity!!.y + offsetY, adjust(mob.z, itemEntity!!.z), speed)
    }

    fun adjust(mobAxis: Double, itemAxis: Double, distance: Double = 1.0): Double {
        if(abs(mobAxis - itemAxis) < 0.25) {
            return itemAxis
        }

        return itemAxis + (if(mobAxis < itemAxis) distance else -distance)
    }
}