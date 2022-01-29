package net.orandja.vw.ai.goal

import net.minecraft.entity.ItemEntity
import net.minecraft.entity.ai.brain.task.LookTargetUtil
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.predicate.entity.EntityPredicates
import java.util.*
import java.util.function.Predicate
import kotlin.math.abs

class TemptItemGoal(protected val mob: AnimalEntity, private val speed: Double,
                    private var foodPredicate: Predicate<ItemStack>, private val offsetY: Int = 1
) :
    Goal() {
    protected var itemEntity: ItemEntity? = null
    private var cooldown = 0
    var isActive = false
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

        mob.entityWorld.getEntitiesByClass(
                ItemEntity::class.java,
                mob.boundingBox.expand(16.0, 4.0, 16.0),
                EntityPredicates.VALID_ENTITY
        ).filter { entity -> foodPredicate.test(entity.stack) }.firstOrNull()?.apply {
            itemEntity = this
            return true
        }

        cooldown = 100
        itemEntity = null
        return false
    }

    override fun shouldContinue(): Boolean {
        return itemEntity != null && mob.squaredDistanceTo(itemEntity) < 36.0 || canStart()
    }

    override fun start() {
        isActive = true
    }

    override fun stop() {
        mob.navigation.stop()
        cooldown = 100
        isActive = false
        itemEntity = null
    }

    override fun tick() {
        mob.lookControl.lookAt(
            itemEntity, (mob.maxHeadRotation + 20).toFloat(),
            mob.maxLookPitchChange.toFloat()
        )

//        LookTargetUtil.lookAtAndWalkTowardsEachOther(mob, itemEntity, speed.toFloat())

        if(itemEntity == null || !itemEntity!!.isAlive) {
            stop()
            return
        }

        if (mob.squaredDistanceTo(itemEntity) < 1.0 && mob.boundingBox.intersects(itemEntity!!.boundingBox)) {
            makeLove()
            stop()
            return
        }

//        mob.navigation.startMovingTo(adjust(mob.x, itemEntity!!.x), itemEntity!!.y + offsetY, adjust(mob.z, itemEntity!!.z), speed)
        LookTargetUtil.walkTowards(mob, itemEntity, speed.toFloat(), 2)
    }

    fun makeLove() {
        if (itemEntity != null) {
            itemEntity!!.stack.decrement(1)
            itemEntity = null
            mob.lovePlayer(null)
        }
    }

    fun adjust(mobAxis: Double, itemAxis: Double, distance: Double = 1.0): Double {
        if(abs(mobAxis - itemAxis) < 0.25) {
            return itemAxis
        }

        return itemAxis + (if(mobAxis < itemAxis) distance else -distance)
    }
}