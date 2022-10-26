package net.orandja.vw.mods.AnimalTemptation;

import com.google.common.collect.Maps;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface AnimalTemptation {

    class TemptItemGoal extends Goal {

        private final AnimalEntity mob;
        private final double speed;
        private final Predicate<ItemStack> foodPredicate;
        private final double offsetY;
        private final StackHandler stackHandler;

        interface StackHandler {
            ItemStack affect(ItemStack stack);
        }

        static final StackHandler defaultHandler = it -> {
            it.decrement(1);
            return it;
        };

        ItemEntity itemEntity = null;
        private int cooldown = 0;
        boolean active = false;

        public TemptItemGoal(AnimalEntity mob, double speed, Predicate<ItemStack> foodPredicate) {
            this(mob, speed, foodPredicate, 1.0D, defaultHandler);
        }

        public TemptItemGoal(AnimalEntity mob, double speed, Predicate<ItemStack> foodPredicate, double offsetY) {
            this(mob, speed, foodPredicate, offsetY, defaultHandler);
        }

        public TemptItemGoal(AnimalEntity mob, double speed, Predicate<ItemStack> foodPredicate, StackHandler stackHandler) {
            this(mob, speed, foodPredicate, 1.0D, stackHandler);
        }

        public TemptItemGoal(AnimalEntity mob, double speed, Predicate<ItemStack> foodPredicate, double offsetY, StackHandler stackHandler) {
            this.mob = mob;
            this.speed = speed;
            this.foodPredicate = foodPredicate;
            this.offsetY = offsetY;
            this.stackHandler = stackHandler;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        public boolean canStart() {
            if (mob.isInLove() || mob.isBaby() || mob.getBreedingAge() > 0) {
                return false;
            }

            if (cooldown > 0) {
                --cooldown;
                return false;
            }

            mob.getEntityWorld().getEntitiesByClass(ItemEntity.class, mob.getBoundingBox().expand(16.0, 4.0, 16.0), EntityPredicates.VALID_ENTITY)
            .stream().filter(entity -> foodPredicate.test(entity.getStack())).findFirst().ifPresentOrElse(entity -> itemEntity = entity, () -> itemEntity = null);

            if(itemEntity == null) {
                cooldown = 100;
                return false;
            } else
                return true;
        }

        public boolean shouldContinue() {
            return (itemEntity != null ? itemEntity.squaredDistanceTo(mob) : 36.1) < 36.0 || canStart();
        }

        public void start() {
            active = true;
        }

        public void stop() {
            mob.getNavigation().stop();
            cooldown = 100;
            active = false;
            itemEntity = null;
        }

        public void tick() {
            if(itemEntity == null || !itemEntity.isAlive()) {
                stop();
                return;
            }

            mob.getLookControl().lookAt(itemEntity, (mob.getMaxHeadRotation() + 20), mob.getMaxLookPitchChange());

            if (mob.squaredDistanceTo(itemEntity) < 1.0 && mob.getBoundingBox().intersects(itemEntity.getBoundingBox())) {
                if (itemEntity != null) {
                    itemEntity.setStack(stackHandler.affect(itemEntity.getStack()));
                    mob.lovePlayer(null);
                }
                stop();
                return;
            }

            mob.getNavigation().startMovingTo(adjust(mob.getX(), itemEntity.getX()), itemEntity.getY() + offsetY, adjust(mob.getZ(), itemEntity.getZ()), speed);
        }

        public double adjust(double mobAxis, double itemAxis) {
            return adjust(mobAxis, itemAxis, 1.0D);
        }

        public double adjust(double mobAxis, double itemAxis, double distance) {
            if(Math.abs(mobAxis - itemAxis) < 0.25D) {
                return itemAxis;
            }

            return itemAxis + (mobAxis < itemAxis ? distance : -distance);
        }
    }

    interface AnimalItemGoal {
        Pair<Integer, TemptItemGoal> getGoal(AnimalEntity entity);
    }

    static void addGoal(GoalSelector selector, int priority, TemptItemGoal goal) {
        selector.add(priority, goal);
    }

    static void addGoal(GoalSelector selector, Pair<Integer, TemptItemGoal> pair) {
        addGoal(selector, pair.getLeft(), pair.getRight());
    }

    Map<Class<? extends AnimalEntity>, AnimalItemGoal> TEMPTATIONS = Maps.newHashMap();

    static void getTemptation(AnimalEntity animal, Consumer<AnimalItemGoal> consumer) {
        TEMPTATIONS.keySet().stream().filter(clazz -> animal.getClass().isAssignableFrom(clazz) || animal.getClass() == clazz)
                .findFirst().ifPresent(animalEntityClass -> consumer.accept(TEMPTATIONS.get(animalEntityClass)));
    }

    static void beforeLaunch() {
        TEMPTATIONS.put(AxolotlEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 0.33, ((AxolotlEntity)it)::isBreedingItem, 0.0, (stack) -> new ItemStack(Items.WATER_BUCKET))));
        TEMPTATIONS.put(BeeEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((BeeEntity)it)::isBreedingItem, 0.0)));
        TEMPTATIONS.put(CatEntity.class, it -> new Pair<>(10, new TemptItemGoal(it, 1.25, ((CatEntity)it)::isBreedingItem, 1.0D)));
        TEMPTATIONS.put(ChickenEntity.class, it -> new Pair<>(10, new TemptItemGoal(it, 1.25, ((ChickenEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(CowEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((CowEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(FoxEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((FoxEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(AbstractHorseEntity.class, it -> new Pair<>(4, new TemptItemGoal(it, 1.25, ((AbstractHorseEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(LlamaEntity.class, it -> new Pair<>(4, new TemptItemGoal(it, 1.25, ((LlamaEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(OcelotEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((OcelotEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(PigEntity.class, it -> new Pair<>(4, new TemptItemGoal(it, 1.25, ((PigEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(RabbitEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((RabbitEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(SheepEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((SheepEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(WolfEntity.class, it -> new Pair<>(9, new TemptItemGoal(it, 1.25, ((WolfEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(TurtleEntity.class, it -> new Pair<>(2, new TemptItemGoal(it, 1.25, ((TurtleEntity)it)::isBreedingItem)));

        TEMPTATIONS.put(StriderEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((StriderEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(GoatEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((GoatEntity)it)::isBreedingItem)));
        TEMPTATIONS.put(HoglinEntity.class, it -> new Pair<>(3, new TemptItemGoal(it, 1.25, ((HoglinEntity)it)::isBreedingItem)));
    }

    default void applyTo(World world, Object entity, GoalSelector goalSelector) {
        if(world != null && !world.isClient && entity instanceof AnimalEntity animal) {
            getTemptation(animal, goalPair -> addGoal(goalSelector, goalPair.getGoal(animal)));
        }
    }
}
