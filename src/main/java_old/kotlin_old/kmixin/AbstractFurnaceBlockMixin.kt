@file:Mixin(AbstractFurnaceBlockEntity::class)
@file:JvmName("AbstractFurnaceBlockEntityMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.AbstractFurnaceBlock
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeUnlocker
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.orandja.vw.maccessors.EnchantMoreAccessor
import net.orandja.vw.maccessors.EnchantMoreBlock
import org.objectweb.asm.Opcodes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.max

@Redirect(
    method = ["tick"],
    at = At(
        value = "FIELD",
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I",
        opcode = Opcodes.PUTFIELD,
        ordinal = 0
    )
)
private fun decreaseBurnTime(entity: AbstractFurnaceBlockEntity, ignored: Int) {
    ((entity as? AbstractFurnaceBlockEntityMixin) ?: return).apply {
        if (isBurning() && (flame == 0.toShort() || !inventory!![0].isEmpty)) {
            this.burnTime = max(0, burnTime - max(1, (efficiency + smite) * 2))
        }
    }
}

@Redirect(
    method = ["tick"],
    at = At(
        value = "FIELD",
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I",
        opcode = Opcodes.PUTFIELD,
        ordinal = 1
    )
)
private fun setBurnTime(entity: AbstractFurnaceBlockEntity, burnTime: Int) {
    ((entity as? AbstractFurnaceBlockEntityMixin) ?: return).apply {
        this.burnTime = burnTime + burnTime * (unbreaking + fireAspect) / 5
    }
}


@Inject(
    method = ["tick"],
    at = [At(
        value = "FIELD",
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;cookTime:I",
        shift = At.Shift.AFTER,
        ordinal = 1
    )]
)
private fun accelerateCookTime(
    world: World,
    pos: BlockPos,
    state: BlockState,
    entity: AbstractFurnaceBlockEntity,
    info: CallbackInfo
) {
    (entity as AbstractFurnaceBlockEntityMixin).apply {
        if (efficiency + smite > 0) cookTime = MathHelper.clamp(
            cookTime - 1 + max(1, (efficiency + smite) * 2), 0, cookTimeTotal
        )
    }

}

@Inject(
    method = ["tick"],
    at = [At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;setLastRecipe(Lnet/minecraft/recipe/Recipe;)V",
        shift = At.Shift.AFTER
    )]
)
private fun increaseOutputAmount(
    world: World,
    pos: BlockPos,
    state: BlockState,
    entity: AbstractFurnaceBlockEntity,
    info: CallbackInfo
) {
    (entity as AbstractFurnaceBlockEntityMixin).apply {
        if (fortune < 1) return
        val itemstack = inventory!![2]
        itemstack.increment(1 * max(0, world!!.random.nextInt(fortune + 2) - 1))
    }
}

@Mixin(AbstractFurnaceBlockEntity::class)
abstract class AbstractFurnaceBlockEntityMixin(
    blockEntityType: BlockEntityType<*>?, blockPos: BlockPos?,
    blockState: BlockState?
) :
    LockableContainerBlockEntity(blockEntityType, blockPos, blockState), SidedInventory, RecipeUnlocker,
    RecipeInputProvider, EnchantMoreAccessor {

    /*
        EFFICIENCY, SMITE -> faster processing, combustible burns faster
        FLAME -> keeps flame from being used while idle.
        UNBREAKING, FIRE_ASPECT -> combustible burns longer
        FORTUNE -> outputs more items on cooking.
     */
    var smite: Short = 0
    var efficiency: Short = 0
    var flame: Short = 0
    var unbreaking: Short = 0
    var fireAspect: Short = 0
    var fortune: Short = 0

    @Shadow
    var burnTime = 0

    @Shadow
    val fuelTime = 0

    @Shadow
    // Accelerate to 200 ticks
    var cookTime = 0

    @Shadow
    // Should always be 200 ticks
    val cookTimeTotal = 0

    @Shadow
    var inventory: DefaultedList<ItemStack>? = null

    @Shadow
    abstract fun isBurning(): Boolean

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "smite" to smite,
            "flame" to flame,
            "unbreaking" to unbreaking,
            "fire_aspect" to fireAspect,
            "fortune" to fortune
        )
    }

    override fun isEnchanted(): Boolean {
        return smite > 0 || efficiency > 0 || flame > 0 || unbreaking > 0 || fireAspect > 0 || fortune > 0
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "smite" -> smite = level
            "efficiency" -> efficiency = level
            "unbreaking" -> unbreaking = level
            "flame" -> flame = level
            "fire_aspect" -> fireAspect = level
            "fortune" -> fortune = level
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

@Mixin(AbstractFurnaceBlock::class)
abstract class AbstractFurnaceBlockMixin(settings: Settings) : BlockWithEntity(settings), EnchantMoreBlock {

    @Inject(at = [At("RETURN")], method = ["onPlaced"])
    override fun placeEnchant(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity,
        stack: ItemStack,
        info: CallbackInfo
    ) {
        super.placeEnchant(world, pos, state, placer, stack, info)
    }

    override fun getDroppedStacks(state: BlockState, builder: LootContext.Builder): List<ItemStack> {
        return super.getDroppedStacks(state, builder).apply {
            applyEnchant(builder.get(LootContextParameters.BLOCK_ENTITY) as EnchantMoreAccessor, this)
        }
    }
}