@file:Mixin(BrewingStandBlockEntity::class)
@file:JvmName("BrewingStandBlockEntityMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.BrewingStandBlock
import net.minecraft.block.entity.BrewingStandBlockEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
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
        target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;fuel:I",
        opcode = Opcodes.PUTFIELD,
        ordinal = 0
    )
)
private fun setFuelCount(blockEntity: BrewingStandBlockEntity, fuel: Int) {
    (blockEntity as BrewingStandBlockEntityMixin).apply {
        this.fuel = fuel + fuel * (unbreaking + fireAspect) / 5
    }
}

@Inject(
    method = ["tick"],
    at = [At(
        value = "FIELD",
        target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;brewTime:I",
        shift = At.Shift.AFTER,
        ordinal = 0
    )]
)
private fun accelerate(
    world: World,
    pos: BlockPos,
    state: BlockState,
    blockEntity: BrewingStandBlockEntity,
    info: CallbackInfo
) {
    (blockEntity as BrewingStandBlockEntityMixin).apply {
        if (efficiency + baneOfArthropods > 0) brewTime = MathHelper.clamp(
            brewTime + 1 - max(
                1,
                (efficiency + baneOfArthropods) * 2
            ), 0, 400
        )
    }
}

@Mixin(BrewingStandBlockEntity::class)
class BrewingStandBlockEntityMixin : EnchantMoreAccessor {

    var baneOfArthropods: Short = 0
    var efficiency: Short = 0
    var unbreaking: Short = 0
    var fireAspect: Short = 0
    var silkTouch: Short = 0


    @Shadow
    var brewTime = 0

    @Shadow
    var fuel = 0

    override fun getEnchantments(): Map<String, Short> {
        return mapOf(
            "efficiency" to efficiency,
            "bane_of_arthropods" to baneOfArthropods,
            "unbreaking" to unbreaking,
            "fire_aspect" to fireAspect,
            "silk_touch" to silkTouch
        )
    }

    override fun isEnchanted(): Boolean {
        return baneOfArthropods > 0 || efficiency > 0 || unbreaking > 0 || fireAspect > 0 || silkTouch > 0
    }

    override fun hasEnchantment(enchantment: Enchantment, level: Short): Boolean {
        when (enchantment) {
            Enchantments.SILK_TOUCH -> return silkTouch >= level
            Enchantments.EFFICIENCY -> return efficiency >= level
            Enchantments.UNBREAKING -> return unbreaking >= level
            Enchantments.FIRE_ASPECT -> return fireAspect >= level
            Enchantments.BANE_OF_ARTHROPODS -> return baneOfArthropods >= level
        }
        return super.hasEnchantment(enchantment, level)
    }

    override fun getProperty(name: String): Any? {
        when(name) {
            "brewTime" -> return brewTime
        }
        return super.getProperty(name)
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "bane_of_arthropods" -> baneOfArthropods = level
            "efficiency" -> efficiency = level
            "unbreaking" -> unbreaking = level
            "fire_aspect" -> fireAspect = level
            "silk_touch" -> silkTouch = level
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

@Mixin(BrewingStandBlock::class)
abstract class BrewingStandBlockMixin(settings: Settings) : BlockWithEntity(settings), EnchantMoreBlock {

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