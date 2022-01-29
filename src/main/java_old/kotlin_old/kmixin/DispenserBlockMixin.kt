@file:Mixin(DispenserBlock::class)
@file:JvmName("DispenserBlockMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.DispenserBlock
import net.minecraft.block.dispenser.DispenserBehavior
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.*
import net.minecraft.world.World
import net.orandja.vw.accessor.DispenserBlockEntityAccessor
import net.orandja.vw.crafting.AutomatedCraftingInventory
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(DispenserBlock::class)
abstract class DispenserBlockMixin(settings: Settings?) : BlockWithEntity(settings) {

    private val BEHAVIOR: DispenserBehavior = object : DispenserBehavior {
        override fun dispense(pointer: BlockPointer, itemStack: ItemStack): ItemStack {
            pointer.world.apply {
                syncWorldEvent(1000, pointer.pos, 0)
                syncWorldEvent(2000, pointer.pos, pointer.blockState.get(DispenserBlock.FACING).id)
            }
            return dispenseSilently(pointer, itemStack)
        }

        protected fun dispenseSilently(pointer: BlockPointer, stack: ItemStack): ItemStack {
            spawnItem(pointer.world, stack, 6, pointer.blockState.get(DispenserBlock.FACING), DispenserBlock.getOutputLocation(pointer))
            return stack
        }

        fun spawnItem(world: World, stack: ItemStack?, offset: Int, side: Direction, pos: Position?) {
            val itemPos = BlockPos(pos).offset(side)
            var e = itemPos.y + 0.5 - (if (side.axis === Direction.Axis.Y) {
                0.125
            } else {
                0.15625
            })

            world.spawnEntity(ItemEntity(world, itemPos.x + 0.5, e, itemPos.z + 0.5, stack).apply stack@{
                world.random.apply {
                    val g = nextDouble() * 0.1 + 0.2
                    this@stack.setVelocity(
                        nextGaussian() * 0.007499999832361937 * offset.toDouble() + side.offsetX.toDouble() * g,
                        nextGaussian() * 0.007499999832361937 * offset.toDouble() + 0.20000000298023224,
                        nextGaussian() * 0.007499999832361937 * offset.toDouble() + side.offsetZ.toDouble() * g
                    )
                }
            })
        }
    }

    @Inject(method = ["dispense"], at = [At("HEAD")], cancellable = true)
    protected open fun dispense(world: ServerWorld, pos: BlockPos, info: CallbackInfo) {
        if (world.getBlockState(pos.offset(world.getBlockState(pos).get(DispenserBlock.FACING))) !== Blocks.CRAFTING_TABLE.defaultState) {
            return
        }

        info.cancel()
        (world.getBlockEntity(pos) as? DispenserBlockEntityAccessor ?: return).apply {
            val stacks = getInventory()
            val inventory = AutomatedCraftingInventory(3, 3, stacks)
            val optional = world.server.recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, world)
            if (optional.isPresent) {
                val craftingRecipe = optional.get()
                val itemStack = craftingRecipe.craft(inventory)
                val blockPointerImpl = BlockPointerImpl(world, pos)
                BEHAVIOR.dispense(blockPointerImpl, itemStack)
                val defaultedList = world.recipeManager.getRemainingStacks(RecipeType.CRAFTING, inventory, world)
                for (i in defaultedList.indices) {
                    var invStack: ItemStack = inventory.getStack(i)
                    val itemStack2 = defaultedList[i]
                    if (!invStack.isEmpty) {
                        inventory.removeStack(i, 1)
                        invStack = inventory.getStack(i)
                    }
                    if (!itemStack2.isEmpty) {
                        if (invStack.isEmpty) {
                            inventory.setStack(i, itemStack2)
                            stacks[i] = itemStack2
                        } else if (ItemStack.areItemsEqualIgnoreDamage(
                                invStack,
                                itemStack2
                            ) && ItemStack.areNbtEqual(invStack, itemStack2)
                        ) {
                            itemStack2.increment(invStack.count)
                            inventory.setStack(i, itemStack2)
                            stacks[i] = itemStack2
                        } else {
                            BEHAVIOR.dispense(blockPointerImpl, itemStack2)
                        }
                    }
                }
            }
        }
    }

}

