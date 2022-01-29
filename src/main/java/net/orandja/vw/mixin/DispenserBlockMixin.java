package net.orandja.vw.mixin;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.orandja.vw.accessor.DispenserBlockEntityAccessor;
import net.orandja.vw.crafting.AutomatedCraftingInventory;
import net.orandja.vw.utils.ItemUtilsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;

import static net.orandja.vw.utils.CatchUtilsKt.castAs;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BlockWithEntity {
    protected DispenserBlockMixin(Settings settings) {
        super(settings);
    }

    private DispenserBehavior BEHAVIOR = new DispenserBehavior() {
        @Override
        public ItemStack dispense(BlockPointer pointer, ItemStack itemStack) {
            World world = pointer.getWorld();
            world.syncWorldEvent(1000, pointer.getPos(), 0);
            world.syncWorldEvent(2000, pointer.getPos(), pointer.getBlockState().get(DispenserBlock.FACING).getId());
            return dispenseSilently(pointer, itemStack);
        }

        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            spawnItem(pointer.getWorld(), stack, 6, pointer.getBlockState().get(DispenserBlock.FACING), DispenserBlock.getOutputLocation(pointer));
            return stack;
        }

        void spawnItem(World world, ItemStack stack, int offset, Direction side, Position pos) {
            BlockPos itemPos = new BlockPos(pos).offset(side);
            var e = itemPos.getY() + 0.5 - ((side.getAxis() == Direction.Axis.Y) ? 0.125 : 0.15625);

            ItemEntity item = new ItemEntity(world, itemPos.getX() + 0.5, e, itemPos.getZ() + 0.5, stack);

            Random random = world.getRandom();
            double g = random.nextDouble() * 0.1 + 0.2;
            item.setVelocity(
                    random.nextGaussian() * 0.007499999832361937 * offset + side.getOffsetX() * g,
                    random.nextGaussian() * 0.007499999832361937 * offset + 0.20000000298023224,
                    random.nextGaussian() * 0.007499999832361937 * offset + side.getOffsetZ() * g
            );
            world.spawnEntity(item);
        }
    };

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    void dispense(ServerWorld world, BlockPos pos, CallbackInfo info) {
        Direction facing = world.getBlockState(pos).get(DispenserBlock.FACING);
        BlockPos craftingPos = pos.offset(facing);
        if (world.getBlockState(craftingPos) != Blocks.CRAFTING_TABLE.getDefaultState()) {
            return;
        }

        info.cancel();
        castAs(DispenserBlockEntity.class, world.getBlockEntity(pos), dispenser -> {
            BlockPos outputPos = pos.offset(facing, 2);

            DefaultedList<ItemStack> stacks = castAs(DispenserBlockEntityAccessor.class, dispenser).getInventory();
            AutomatedCraftingInventory inventory = new AutomatedCraftingInventory(3, 3, stacks);
            Optional<CraftingRecipe> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventory, world);
            if (!optional.isPresent()) {
                return;
            }

            CraftingRecipe craftingRecipe = optional.get();
            Inventory outputInventory = HopperBlockEntity.getInventoryAt(world, outputPos);
            ItemStack craftedStack = null;
            BlockPointer blockPointerImpl = new BlockPointerImpl(world, pos);
            if (outputInventory != null) {
                ItemStack recipeOutput = craftingRecipe.getOutput().copy();

                for(int i = 0; i < outputInventory.size(); i++) {
                    ItemStack outputStack = outputInventory.getStack(i);
                    if(outputStack.isEmpty()) {
                        craftedStack = craftingRecipe.craft(inventory);
                        outputInventory.setStack(i, craftedStack);
                        break;
                    } else if(ItemUtilsKt.canMergeItems(outputStack, recipeOutput, recipeOutput.getCount())) {
                        craftedStack = craftingRecipe.craft(inventory);
                        outputStack.increment(craftedStack.getCount());
                        break;
                    }
                }
            } else {
                craftedStack = craftingRecipe.craft(inventory);
                BEHAVIOR.dispense(blockPointerImpl, craftedStack);
            }

            if(craftedStack == null) {
                return;
            }

            DefaultedList<ItemStack> defaultedList = world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, inventory, world);
            for(int i = 0; i < defaultedList.size(); i++) {
                ItemStack invStack = inventory.getStack(i);
                ItemStack itemStack2 = defaultedList.get(i);
                if (!invStack.isEmpty()) {
                    inventory.removeStack(i, 1);
                    invStack = inventory.getStack(i);
                }

                if (!itemStack2.isEmpty()) {
                    if (invStack.isEmpty()) {
                        inventory.setStack(i, itemStack2);
                        stacks.set(i, itemStack2);
                    } else if (ItemStack.areItemsEqualIgnoreDamage(invStack, itemStack2) && ItemStack.areNbtEqual(invStack, itemStack2)) {
                        itemStack2.increment(invStack.getCount());
                        inventory.setStack(i, itemStack2);
                        stacks.set(i, itemStack2);
                    } else {
                        BEHAVIOR.dispense(blockPointerImpl, itemStack2);
                    }
                }
            }
        });
    }
}
