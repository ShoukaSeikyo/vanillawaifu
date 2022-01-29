@file:Mixin(AbstractFurnaceBlockEntity::class)
@file:JvmName("AbstractFurnaceBlockEntityMixin2")

package net.orandja.vw.mixin

import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeUnlocker
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.orandja.vw.maccessors.EnchantMoreAccessor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(AbstractFurnaceBlockEntity::class)
abstract class AbstractFurnaceBlockEntityMixin2(
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