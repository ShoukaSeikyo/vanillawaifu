package net.orandja.vw.logic

import net.minecraft.block.Blocks
import net.minecraft.block.DispenserBlock
import net.minecraft.block.dispenser.DispenserBehavior
import net.minecraft.block.entity.DispenserBlockEntity
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.*
import net.minecraft.world.World
import net.orandja.mcutils.canMerge
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class AutomatedCraftingInventory(private val width: Int, private val height: Int, stacks: List<ItemStack>) :
    CraftingInventory(null, width, height) {
    private val stacks: DefaultedList<ItemStack> =
        DefaultedList.ofSize(width * height, ItemStack.EMPTY).apply { stacks.indices.forEach { this[it] = stacks[it] } }

    override fun size(): Int = this.stacks.size

    override fun isEmpty(): Boolean = this.stacks.firstOrNull { !it.isEmpty } == null
    override fun getStack(slot: Int): ItemStack = if (slot >= size()) ItemStack.EMPTY else this.stacks[slot]
    override fun setStack(slot: Int, stack: ItemStack) {
        this.stacks[slot] = stack
    }

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.stacks, slot)
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(this.stacks, slot, amount)

    override fun markDirty() {}
    override fun canPlayerUse(player: PlayerEntity): Boolean = true
    override fun clear() = this.stacks.clear()
    override fun getHeight(): Int = this.height
    override fun getWidth(): Int = this.width
    override fun provideRecipeInputs(finder: RecipeMatcher) = this.stacks.forEach(finder::addInput)
}

private val BEHAVIOR: DispenserBehavior = object : DispenserBehavior {
    override fun dispense(pointer: BlockPointer, itemStack: ItemStack): ItemStack {
        val world: World = pointer.world
        world.syncWorldEvent(1000, pointer.pos, 0)
        world.syncWorldEvent(2000, pointer.pos, pointer.blockState.get(DispenserBlock.FACING).id)
        return dispenseSilently(pointer, itemStack)
    }

    protected fun dispenseSilently(pointer: BlockPointer, stack: ItemStack): ItemStack {
        spawnItem(
            pointer.world,
            stack,
            6,
            pointer.blockState.get(DispenserBlock.FACING),
            DispenserBlock.getOutputLocation(pointer)
        )
        return stack
    }

    fun spawnItem(world: World, stack: ItemStack?, offset: Int, side: Direction, pos: Position?) {
        val itemPos = BlockPos(pos).offset(side)
        val yOffset = itemPos.y + 0.5 - if (side.axis === Direction.Axis.Y) 0.125 else 0.15625
        val itemEntity = ItemEntity(world, itemPos.x + 0.5, yOffset, itemPos.z + 0.5, stack)
        val random = world.getRandom()
        val randomOffset = random.nextDouble() * 0.1 + 0.2
        itemEntity.setVelocity(
            random.nextGaussian() * 0.007499999832361937 * offset + side.offsetX * randomOffset,
            random.nextGaussian() * 0.007499999832361937 * offset + 0.20000000298023224,
            random.nextGaussian() * 0.007499999832361937 * offset + side.offsetZ * randomOffset
        )
        world.spawnEntity(itemEntity)
    }
}

interface CraftingDispenserBlock {

    var inventory: DefaultedList<ItemStack>

    fun onBlockDispense(world: ServerWorld, pos: BlockPos, info: CallbackInfo) {
        val facing = world.getBlockState(pos).get(DispenserBlock.FACING)
        if (world.getBlockState(pos.offset(facing)) !== Blocks.CRAFTING_TABLE.defaultState) {
            return
        }

        info.cancel()

        val dispenser: DispenserBlockEntity = world.getBlockEntity(pos) as? DispenserBlockEntity ?: return
        val outputPos = pos.offset(facing, 2)

        val stacks = (dispenser as? CraftingDispenserBlock)?.inventory ?: return
        val inventory = AutomatedCraftingInventory(3, 3, stacks)
        val optional = world.recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, world)
        if (!optional.isPresent)
            return

        val craftingRecipe = optional.get()
        val outputInventory = HopperBlockEntity.getInventoryAt(world, outputPos)
        var craftedStack: ItemStack? = null
        val blockPointerImpl: BlockPointer = BlockPointerImpl(world, pos)
        if (outputInventory != null) {
            val recipeOutput = craftingRecipe.output.copy()
            for (i in 0 until outputInventory.size()) {
                if(outputInventory.isValid(i, recipeOutput)) {
                    val outputStack = outputInventory.getStack(i)
                    if (outputStack.isEmpty) {
                        craftedStack = craftingRecipe.craft(inventory)
                        outputInventory.setStack(i, craftedStack)
                        break
                    } else if (outputStack.canMerge(recipeOutput, recipeOutput.count)) {
                        craftedStack = craftingRecipe.craft(inventory)
                        outputStack.increment(craftedStack.count)
                        break
                    }
                }
            }
        } else {
            craftedStack = craftingRecipe.craft(inventory)
            BEHAVIOR.dispense(blockPointerImpl, craftedStack)
        }
        if (craftedStack == null) {
            return
        }
        val defaultedList = world.recipeManager.getRemainingStacks(RecipeType.CRAFTING, inventory, world)

        for (i in defaultedList.indices) {
            var invStack = inventory.getStack(i)
            val itemStack2 = defaultedList[i]
            if (!invStack.isEmpty) {
                inventory.removeStack(i, 1)
                invStack = inventory.getStack(i)
            }
            if (!itemStack2.isEmpty) {
                if (invStack.isEmpty) {
                    inventory.setStack(i, itemStack2)
                    stacks[i] = itemStack2
                } else if (ItemStack.areItemsEqualIgnoreDamage(invStack, itemStack2) && ItemStack.areNbtEqual(
                        invStack,
                        itemStack2
                    )
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