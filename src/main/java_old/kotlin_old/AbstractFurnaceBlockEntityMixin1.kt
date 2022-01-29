@file:Mixin(AbstractFurnaceBlockEntity::class)
@file:JvmName("AbstractFurnaceBlockEntityMixin1")

package net.orandja.vw.mixin

import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import org.objectweb.asm.Opcodes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Redirect(
    method = ["tick"],
    at = At(
        value = "FIELD",
        target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I",
        opcode = Opcodes.PUTFIELD,
        ordinal = 0
    )
)
private fun decreaseBurnTime(blockEntity: AbstractFurnaceBlockEntity, ignored: Int) {
    (blockEntity as AbstractFurnaceBlockEntityMixin2).apply {
        if (isBurning()) {
            if (flame > 0 && inventory!![0].isEmpty) {
                return
            }
            burnTime = Math.max(0, burnTime - 1)
        }
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
    (entity as AbstractFurnaceBlockEntityMixin2).apply {
        if (efficiency + smite > 0) cookTime = MathHelper.clamp(
            cookTime - 1 + Math.max(1, (efficiency + smite) * 2), 0, cookTimeTotal
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
    val furnace = entity as AbstractFurnaceBlockEntityMixin2
    if (furnace.fortune < 1) return
    val itemstack = furnace.inventory!![2]
    itemstack.increment(1 * Math.max(0, furnace.world!!.random.nextInt(furnace.fortune + 2) - 1))
}