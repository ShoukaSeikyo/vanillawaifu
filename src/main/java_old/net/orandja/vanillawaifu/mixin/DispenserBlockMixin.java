package net.orandja.vanillawaifu.mixin;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.crafting.AutomatedCraftingInventory;
import net.orandja.vanillawaifu.utils.BlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BlockWithEntity {

    private final DispenserBehavior BEHAVIOR = new DispenserBehavior() {
        public final ItemStack dispense(BlockPointer pointer, ItemStack itemStack) {
            ItemStack itemStack2 = this.dispenseSilently(pointer, itemStack);
            pointer.getWorld().syncWorldEvent(1000, pointer.getBlockPos(), 0);
            pointer.getWorld().syncWorldEvent(2000, pointer.getBlockPos(), pointer.getBlockState().get(DispenserBlock.FACING).getId());
            return itemStack2;
        }

        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
            Position position = DispenserBlock.getOutputLocation(pointer);
            //ItemStack itemStack = stack.split(1);
            spawnItem(pointer.getWorld(), stack, 6, direction, position);
            return stack;
        }

        public void spawnItem(World world, ItemStack stack, int offset, Direction side, Position pos) {
            BlockPos itemPos = new BlockPos(pos).offset(side);
            double e = itemPos.getY() + 0.5D;
            if (side.getAxis() == Direction.Axis.Y) {
                e -= 0.125D;
            } else {
                e -= 0.15625D;
            }

            ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5D, e, itemPos.getZ() + 0.5D, stack);
            double g = world.random.nextDouble() * 0.1D + 0.2D;
            itemEntity.setVelocity(world.random.nextGaussian() * 0.007499999832361937D * (double)offset + (double)side.getOffsetX() * g, world.random.nextGaussian() * 0.007499999832361937D * (double)offset + 0.20000000298023224D, world.random.nextGaussian() * 0.007499999832361937D * (double)offset + (double)side.getOffsetZ() * g);
            world.spawnEntity(itemEntity);
        }
    };

    protected DispenserBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    protected void dispense(ServerWorld world, BlockPos pos, CallbackInfo info) {
        Direction direction = world.getBlockState(pos).get(DispenserBlock.FACING);
        if (world.getBlockState(pos.offset(direction)) == Blocks.CRAFTING_TABLE.getDefaultState()) {
            info.cancel();
            if (world.getBlockEntity(pos) instanceof DispenserBlockEntity) {
                DefaultedList<ItemStack> stacks = BlockUtils.access(world, pos, "inventory");
                AutomatedCraftingInventory inv = new AutomatedCraftingInventory(3, 3, stacks);

                Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inv, world);
                if (optional.isPresent()) {
                    CraftingRecipe craftingRecipe = optional.get();
                    ItemStack itemStack = craftingRecipe.craft(inv);
                    BlockPointerImpl blockPointerImpl = new BlockPointerImpl(world, pos);
                    BEHAVIOR.dispense(blockPointerImpl, itemStack);


                    DefaultedList<ItemStack> defaultedList = world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, inv, world);

                    for (int i = 0; i < defaultedList.size(); ++i) {
                        ItemStack invStack = inv.getStack(i);
                        ItemStack itemStack2 = defaultedList.get(i);
                        if (!invStack.isEmpty()) {
                            inv.removeStack(i, 1);
                            invStack = inv.getStack(i);
                        }

                        if (!itemStack2.isEmpty()) {
                            if (invStack.isEmpty()) {
                                inv.setStack(i, itemStack2);
                                stacks.set(i, itemStack2);
                            } else if (ItemStack.areItemsEqualIgnoreDamage(invStack, itemStack2) && ItemStack.areTagsEqual(invStack, itemStack2)) {
                                itemStack2.increment(invStack.getCount());
                                inv.setStack(i, itemStack2);
                                stacks.set(i, itemStack2);
                            } else {
                                BEHAVIOR.dispense(blockPointerImpl, itemStack2);
                            }
                        }
                    }

                    //world.spawnEntity(new ItemEntity(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), itemStack));
                }
            }
        }
    }
}
