package net.orandja.vw.logic

import net.minecraft.block.BarrelBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import net.orandja.mcutils.*
import net.orandja.vw.accessor.ItemFrameEntityAccessor
import net.orandja.vw.logic.EnchantMore.Companion.addComplex
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

class DeepStorageSlot(inventory: Inventory, val inventoryIndex: Int, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean {
        return this.inventory.getStack(0).isItemEqual(stack)
    }

    override fun getStack(): ItemStack {
        return inventory.getStack(inventoryIndex)
    }

    override fun setStack(stack: ItemStack) {
        inventory.setStack(inventoryIndex, stack)
        markDirty()
    }

    override fun takeStack(amount: Int): ItemStack {
        return inventory.removeStack(inventoryIndex, amount)
    }
}

class DeepStorageScreenHandler(syncId: Int, playerInventory: PlayerInventory, val inventory: Inventory) : ScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId) {

    override fun canUse(player: PlayerEntity): Boolean {
        return inventory.canPlayerUse(player)
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot: Slot = slots[index]
        if (!slot.hasStack()) {
            return ItemStack.EMPTY
        }

        val itemStack2 = slot.stack
        val itemStack = itemStack2.copy()
        if (index < 27) {
            if (!insertItem(itemStack2, 27, slots.size, true)) {
                return ItemStack.EMPTY
            }
        } else if (!insertItem(itemStack2, 0, 27, false)) {
            return ItemStack.EMPTY
        }

        slot.markDirtyIfEmpty(itemStack2)

        return itemStack
    }

    override fun close(player: PlayerEntity) {
        super.close(player)
        inventory.onClose(player)
    }

    init {
        checkSize(inventory, 27)
        inventory.onOpen(playerInventory.player)

        grid(9, 2) { x, y -> addSlot(DeepStorageSlot(inventory, x + (y * 9), x + (y * 9), 8 + x * 18, 18 + y * 18)) }
        grid(9) { x, _ -> addSlot(DeepStorageSlot(inventory, (inventory.size() - 9) + x, x + 18, 8 + x * 18, 18 + (2 * 18))) }
        grid(9, 3) { x, y -> addSlot(Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18)) }
        grid(9) { x, _ -> addSlot(Slot(playerInventory, x, 8 + x * 18, 142)) }
    }
}

class MovingInventory(
    val movingSize: Int,
    val displaySize: Int,
    val defaultEntry: ItemStack,
    val delegate: MutableList<ItemStack>
) : DefaultedList<ItemStack>(delegate, defaultEntry) {

    companion object {
        fun ofSize(movingSize: Int, displaySize: Int, defaultEntry: ItemStack): MovingInventory {
            return MovingInventory(movingSize, displaySize, defaultEntry, arrayListOf(*Array<ItemStack>(movingSize) { ItemStack.EMPTY }))
        }
    }

    override fun set(index: Int, element: ItemStack): ItemStack {
        if(element.isEmpty) {
            delegate.removeAt(index)
            delegate.add(defaultEntry)
            return delegate.set(movingSize - 1, defaultEntry)
        } else {
            var iIndex = index
            while(iIndex > 0 && delegate[iIndex - 1].isEmpty) {
                iIndex--
            }
            return delegate.set(iIndex, element)
        }
    }
}

interface DeepBarrelBlock : BlockWithEnchantment {

    var infinity: Short
    var efficiency: Short
    var inventory: DefaultedList<ItemStack>


    override fun hasEnchantments(): Boolean {
        return infinity > 0 || efficiency > 0
    }

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "infinity" to infinity
        )
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "infinity" -> infinity = level
            "efficiency" -> efficiency = level
        }
        if (this.infinity > 0) {
            this.inventory = MovingInventory.ofSize(27 + 27 * (infinity + efficiency), 27, ItemStack.EMPTY)
        }
    }

    fun createBarrelScreenHandler(
        barrel: Any,
        syncId: Int,
        playerInventory: PlayerInventory,
        info: CallbackInfoReturnable<ScreenHandler>
    ) {
        if (this.infinity > 0) {
            info.returnValue = DeepStorageScreenHandler(syncId, playerInventory, barrel as BarrelBlockEntity)
        }
    }

    fun saveEnchantments(tag: NbtCompound, info: CallbackInfo, superWrite: Consumer<NbtCompound>) {
        if (infinity > 0) {
            saveEnchantments(tag)
            this.inventory.toNBT(tag)
            superWrite.accept(tag)
            info.cancel()
        }
    }

    fun loadEnchantments(tag: NbtCompound, info: CallbackInfo, superRead: Consumer<NbtCompound>) {
        loadEnchantments(tag)
        if (infinity > 0) {
            this.inventory = MovingInventory.ofSize(27 + 27 * (infinity + efficiency), 27, ItemStack.EMPTY)
            this.inventory.fromNBT(tag)
            superRead.accept(tag)
            info.cancel()
        }
    }

    fun getBarrelSize(info: CallbackInfoReturnable<Int>) {
        info.returnValue = inventory.size
    }

    fun isValidForBarrel(slot: Int, stack: ItemStack): Boolean {
        return !this.hasEnchantments() || (Block.getBlockFromItem(stack.item) !is ShulkerBoxBlock && stack.isItemEqual(this.inventory[0]))
//                areStacksCompatible(
//            this.inventory[0],
//            stack
//        ))
    }

    companion object {
        val BOX = Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

        fun tick(world: World, pos: BlockPos, state: BlockState, barrel: BarrelBlockEntity) {
            if (world.time % 30L != 0L)
                return

            val box = BOX.offset(pos.offset(state.get(BarrelBlock.FACING)))
            val itemFrame = world.getNonSpectatingEntities(ItemFrameEntity::class.java, box).firstOrNull() ?: return

            barrel as DeepBarrelBlock
            if (itemFrame.heldItemStack.isOfItem(barrel.getStack(0))) {
                itemFrame.heldItemStack.setCustomName(
                    Text.Serializer.fromJson("""{"text":"${barrel.inventory.wholeCount()} "}""")!!
                        .append(barrel.getStack(0).name)
                )
                val copyStack = itemFrame.heldItemStack.copy()
                copyStack.holder = itemFrame
                itemFrame.dataTracker.set((itemFrame as ItemFrameEntityAccessor).iteM_STACK, copyStack)
            }
        }

        fun beforeLaunch() {
            addComplex(
                item = Items.BARREL
            ) { enchantment, stack ->
                if(stack.count > 1) {
                    return@addComplex false
                }

                when (enchantment) {
                    Enchantments.INFINITY -> return@addComplex true
                    Enchantments.EFFICIENCY -> return@addComplex EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0 && stack.hasEnchantments();
                }

                return@addComplex false
            }
        }
    }

}