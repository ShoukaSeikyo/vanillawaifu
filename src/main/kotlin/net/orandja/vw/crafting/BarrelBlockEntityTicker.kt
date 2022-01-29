package net.orandja.vw.crafting

import net.minecraft.block.BarrelBlock
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import net.orandja.vw.accessor.ItemFrameEntityAccessor
import net.orandja.vw.maccessors.BarrelInventoryAccessor
import net.orandja.vw.utils.castAs
import net.orandja.vw.utils.isOfItem
import net.orandja.vw.utils.wholeCount


val BOX = Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

fun tick(world: World, pos: BlockPos, state: BlockState, barrel: BarrelBlockEntity) {
    if (world.time % 30L == 0L) {
        val box = BOX.offset(pos.offset(state.get(BarrelBlock.FACING)))
        world.getNonSpectatingEntities(ItemFrameEntity::class.java, box).firstOrNull()?.apply frame@{
            castAs(BarrelInventoryAccessor::class.java, barrel) {
                if(this.heldItemStack.isOfItem(barrel.getStack(0))) {
                    this.heldItemStack.setCustomName(Text.Serializer.fromJson("""{"text":"${it.getInventory().wholeCount()} "}""")!!.append(barrel.getStack(0).name))
                    val copyStack = heldItemStack.copy()
                    copyStack.holder = this

                    dataTracker.set((this as ItemFrameEntityAccessor).iteM_STACK, copyStack)
                }
            }
        }
    }
}