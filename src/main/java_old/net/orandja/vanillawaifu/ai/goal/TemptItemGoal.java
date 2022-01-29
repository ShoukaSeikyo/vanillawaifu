package net.orandja.vanillawaifu.ai.goal;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class TemptItemGoal extends Goal {
    protected final AnimalEntity mob;
    private final double speed;

    protected ItemEntity itemEntity;
    private int cooldown;
    private boolean active;
    private Ingredient food;
    private Predicate<Item> foodPredicate;
    private Predicate<AnimalEntity> predicate;

    public TemptItemGoal(AnimalEntity mob, double speed, Predicate<Item> foodPredicate) {
        this.mob = mob;
        this.speed = speed;
        this.foodPredicate = foodPredicate;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(mob.getNavigation() instanceof MobNavigation) && !(mob.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    public TemptItemGoal(AnimalEntity mob, double speed, Ingredient food) {
        this.mob = mob;
        this.speed = speed;
        this.food = (Ingredient) food;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(mob.getNavigation() instanceof MobNavigation) && !(mob.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    public TemptItemGoal(AnimalEntity mob, double speed, Ingredient food, Predicate<AnimalEntity> predicate) {
        this(mob, speed, food);
        this.predicate = predicate;
    }

    public TemptItemGoal(AnimalEntity mob, double speed, Predicate<Item> foodPredicate, Predicate<AnimalEntity> predicate) {
        this(mob, speed, foodPredicate);
        this.predicate = predicate;
    }

    @Override
    public boolean canStart() {
        if(predicate != null && !predicate.test(this.mob)) {
            return false;
        }

        if(this.mob.isInLove() || this.mob.isBaby() || this.mob.getBreedingAge() > 0) {
            return false;
        }

        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }


        AtomicBoolean returnValue = new AtomicBoolean(false);
        this.mob.getEntityWorld().getEntitiesByClass(ItemEntity.class, this.mob.getBoundingBox().expand(16.0D, 4.0D, 16.0D), EntityPredicates.VALID_ENTITY).stream().filter(this::isTemptedBy).findFirst().ifPresent(itemEntity -> {
            returnValue.set(true);
            this.itemEntity = itemEntity;
        });

        if(!returnValue.get()) {
            this.cooldown = 100;
        }

        return returnValue.get();
    }

    protected boolean isTemptedBy(ItemEntity entity) {
        return this.foodPredicate != null ? this.foodPredicate.test(entity.getStack().getItem()) : this.food.test(entity.getStack());
    }

    @Override
    public boolean shouldContinue() {
        return (this.itemEntity != null && this.mob.squaredDistanceTo(this.itemEntity) < 36.0D) || this.canStart();
    }

    @Override
    public void start() {
        this.active = true;
    }

    public void stop() {
        this.mob.getNavigation().stop();
        this.cooldown = 100;
        this.active = false;
    }

    public void tick() {
        this.mob.getLookControl().lookAt(this.itemEntity, (float) (this.mob.getBodyYawSpeed() + 20), (float) this.mob.getLookPitchSpeed());
        if (this.mob.squaredDistanceTo(this.itemEntity) < 1.0D && this.mob.getBoundingBox().intersects(this.itemEntity.getBoundingBox())) {
            this.makeLove();
            this.stop();
        } else {
            this.mob.getNavigation().startMovingTo(this.itemEntity, this.speed);
        }

    }

    public void makeLove() {
        if(this.itemEntity != null) {
            this.itemEntity.getStack().decrement(1);
            this.itemEntity = null;
            this.mob.lovePlayer(null);
        }
    }

    public boolean isActive() {
        return this.active;
    }
}