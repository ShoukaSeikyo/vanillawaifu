@file:Mixin(HopperBlockEntity::class)
@file:JvmName("HopperBlockMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.HopperBlock
import net.minecraft.block.entity.Hopper
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.orandja.vw.maccessors.EnchantMoreAccessor
import net.orandja.vw.maccessors.EnchantMoreBlock
import net.orandja.vw.mods.ProtectBlock
import net.orandja.vw.utils.canMergeItems
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.BooleanSupplier
import java.util.stream.IntStream
import kotlin.math.max
import net.minecraft.state.property.Property as Property1

@Inject(
    method = ["extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z"],
    at = [At("HEAD")],
    cancellable = true
)
private fun extract(world: World, hopper: Hopper, info: CallbackInfoReturnable<Boolean>) {
    if (ProtectBlock.preventsExtract(world, hopper.hopperX, hopper.hopperY + 1.0, hopper.hopperZ)) {
        info.returnValue = false
    }
}

@Inject(
    method = ["insertAndExtract"],
    at = [At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/entity/HopperBlockEntity;setCooldown(I)V",
        shift = At.Shift.AFTER
    )]
)
private fun insertAndExtract(world: World, pos: BlockPos, state: BlockState, blockEntity: HopperBlockEntity, booleanSupplier: BooleanSupplier, info: CallbackInfoReturnable<Boolean>
) {
    (blockEntity as HopperBlockEntityMixin).apply {
        val cooldown =
            if (isEnchanted()) transferCooldown - ((transferCooldown.toDouble() - 2) / 5.0 * efficiency.toDouble()).toInt()
            else transferCooldown
        setCooldown(max(1, cooldown))
    }
}

@Redirect(
    method = ["insert"],
    at = At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0)
)
private fun filter(stack: ItemStack, world: World, pos: BlockPos, state: BlockState, inventory: Inventory): Boolean {
    ((world.getBlockEntity(pos) as? HopperBlockEntityMixin) ?: return stack.isEmpty).apply {
        return stack.isEmpty || silkTouch > 0 && stack.count == 1
    }
}

@Redirect(
    method = ["extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z"],
    at = At(
        value = "INVOKE",
        target = "isInventoryEmpty(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Z"
    )
)
private fun filterIsInventoryEmpty(inventory: Inventory, facing: Direction): Boolean {
    val isFilter: Boolean = ((inventory as? HopperBlockEntityMixin)?.silkTouch ?: 0) > 0
    return getAvailableSlots(inventory, facing)?.allMatch { i ->
        inventory.getStack(i).isEmpty() || isFilter && inventory.getStack(i).count == 1
    } ?: return false
}

@Redirect(
    method = ["extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z"],
    at = At(
        value = "INVOKE",
        target = "getAvailableSlots(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/util/math/Direction;)Ljava/util/stream/IntStream;"
    )
)
private fun filterGetAvailableSlots(inventory: Inventory, facing: Direction): IntStream {
    val isFilter: Boolean = ((inventory as? HopperBlockEntityMixin)?.silkTouch ?: 0) > 0
    val slots = if (inventory is SidedInventory) IntStream.of(*inventory.getAvailableSlots(facing)) else IntStream.range(0, inventory.size());
    return slots.filter { i ->
        !(inventory.getStack(i).isEmpty() || isFilter && inventory.getStack(i).count == 1)
    }
}

@Shadow
private fun getAvailableSlots(inventory: Inventory, side: Direction): IntStream? {
    return null
}


@Redirect(
    method = ["insertAndExtract"],
    at = At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"
    )
)
private fun redstoneEnabled(
    state2: BlockState,
    value: Property1<Boolean>,
    world: World,
    pos: BlockPos,
    state: BlockState,
    blockEntity: HopperBlockEntity,
    booleanSupplier: BooleanSupplier
): Comparable<*> {
    ((world.getBlockEntity(pos) as? HopperBlockEntityMixin)
        ?: return (state.get(HopperBlock.ENABLED) as Boolean)).apply {
        return mending.toInt() == 1 && !state.get(HopperBlock.ENABLED) || state.get(HopperBlock.ENABLED)
    }
}


@Inject(method = ["insert"], at = [At("HEAD")], cancellable = true)
private fun insert(
    world: World,
    pos: BlockPos,
    state: BlockState,
    inventory: Inventory,
    info: CallbackInfoReturnable<Boolean>
) {
    ((world.getBlockEntity(pos) as? HopperBlockEntityMixin) ?: return).apply {
        if (mending.toInt() == 0) {
            return
        }

        val toInv: Inventory = getOutputInventory(world, pos, state) ?: run {
            info.returnValue = false
            return
        }

        val power: Int = world.getReceivedRedstonePower(pos)
        if (power >= toInv.size()) {
            info.returnValue = false
            return
        }

        for (i in 0 until inventory.size()) {
            val stackFromInv: ItemStack = inventory.getStack(i)
            if (!stackFromInv.isEmpty) {
                val stackToInv = toInv.getStack(power)
                if (stackToInv.isEmpty) {
                    toInv.setStack(power, Inventories.splitStack(getInvStackList(), i, 1))
                    info.returnValue = true
                    return
                } else if (stackToInv.isEmpty || canMergeItems(stackToInv, stackFromInv)) {
                    stackFromInv.decrement(1)
                    stackToInv.increment(1)
                    inventory.setStack(i, stackFromInv)
                    toInv.setStack(power, stackToInv)
                    info.returnValue = true
                    return
                }
            }
        }
        info.returnValue = false
        return
    }
}

@Shadow
private fun getOutputInventory(world: World, pos: BlockPos, state: BlockState): Inventory? {
    return null
}

@Mixin(HopperBlockEntity::class)
abstract class HopperBlockEntityMixin : EnchantMoreAccessor {

    @Shadow
    abstract fun setCooldown(cooldown: Int)

    @Shadow
    abstract fun getInvStackList(): DefaultedList<ItemStack>

    @Shadow
    open val transferCooldown = 0

    var mending: Short = 0
    var silkTouch: Short = 0
    var efficiency: Short = 0

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "silk_touch" to silkTouch,
            "mending" to mending,
        )
    }

    override fun isEnchanted(): Boolean {
        return efficiency > 0 || silkTouch > 0 || mending > 0
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "efficiency" -> efficiency = level
            "silk_touch" -> silkTouch = level
            "mending" -> mending = level
        }
    }

    @Inject(method = ["readNbt"], at = [At("HEAD")])
    override fun fromTag(tag: NbtCompound, info: CallbackInfo) {
        super.fromTag(tag, info)
    }

    @Inject(method = ["writeNbt"], at = [At("HEAD")])
    override fun toTag(tag: NbtCompound, info: CallbackInfoReturnable<NbtCompound>) {
        super.toTag(tag, info)
    }
}

@Mixin(HopperBlock::class)
abstract class HopperBlockMixin(settings: Settings) : BlockWithEntity(settings), EnchantMoreBlock {

    @Inject(at = [At("RETURN")], method = ["onPlaced"])
    override fun placeEnchant(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack, info: CallbackInfo) {
        super.placeEnchant(world, pos, state, placer, stack, info)
    }

    override fun getDroppedStacks(state: BlockState, builder: LootContext.Builder): List<ItemStack> {
        return super.getDroppedStacks(state, builder).apply {
            applyEnchant(builder.get(LootContextParameters.BLOCK_ENTITY) as EnchantMoreAccessor, this)
        }
    }
}