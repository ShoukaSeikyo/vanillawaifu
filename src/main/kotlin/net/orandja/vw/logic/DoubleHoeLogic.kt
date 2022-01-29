package net.orandja.vw.logic

import com.google.common.collect.Lists
import net.minecraft.block.*
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.HoeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.orandja.vw.mods.DoubleTools
import net.orandja.vw.mods.anyToolBreaking
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

interface DoubleHoeLogic {

    fun use(context: ItemUsageContext, info: CallbackInfoReturnable<ActionResult>) {
        val world = context.world
        if (world.isClient) {
            return
        }

        val pos = context.blockPos
        val player: PlayerEntity = context.player!!
        val hand = context.hand
        val state = world.getBlockState(pos)
        val block = world.getBlockState(pos).block

        if (block is Fertilizable) {
            if(block is CropBlock && block.isMature(state)) {
                if (!DoubleTools.areBothTools(player, HoeItem::class.java)) {
                    world.setBlockState(pos, block.withAge(0), 2)
                    Block.dropStacks(state, world, pos, null, player, player!!.getStackInHand(hand))
                    context.stack.damage(1, player) { p: PlayerEntity -> p.sendToolBreakStatus(context.hand) }

                    info.setReturnValue(ActionResult.SUCCESS)
                    return
                }

                val direction = Direction.getEntityFacingOrder(player)[0]
                var i = 0
                var nPos: BlockPos? = null
                var nState: BlockState? = null
                val toRemove: MutableList<BlockPos> = Lists.newArrayList()
                val mainTool = player!!.mainHandStack
                val offTool = player!!.offHandStack
                while (i < 16 && world.getBlockState(pos.offset(direction, i).also { nPos = it }).also { nState = it }.block === block) {
                    if (player.anyToolBreaking()) {
                        break
                    }
                    if (block.isMature(nState!!)) {
                        toRemove.add(nPos!!)
                        player!!.mainHandStack.damage(1, player) { p: PlayerEntity -> p.sendToolBreakStatus(Hand.MAIN_HAND) }
                        player!!.offHandStack.damage(1, player) { p: PlayerEntity -> p.sendToolBreakStatus(Hand.OFF_HAND) }
                    }
                    i++
                }
                val dropMap = HashMap<Item, ItemStack>()
                toRemove.forEach(Consumer { cropPos: BlockPos? ->
                    world.setBlockState(cropPos, block.withAge(0), 2)
                    Block.getDroppedStacks(state, world as ServerWorld, pos, null, player, mainTool).forEach(Consumer { stack: ItemStack ->
                        if (dropMap.containsKey(stack.item)) {
                            val otherStack = dropMap[stack.item]
                            var count = otherStack!!.count + stack.count
                            if (count > otherStack!!.maxCount) {
                                otherStack!!.count = otherStack!!.maxCount
                                Block.dropStack(world, pos, otherStack!!.copy())
                                count -= otherStack!!.maxCount
                            }
                            stack.count = count
                        }
                        dropMap[stack.item] = stack
                    })
                })
                ExperienceOrbEntity.spawn(world as ServerWorld, Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()), toRemove.size - 1)
                dropMap.values.forEach(Consumer { stack: ItemStack? -> Block.dropStack(world, pos, stack) })
                state.onStacksDropped(world, pos, mainTool)

                info.setReturnValue(ActionResult.SUCCESS)
                return
            }


            if (block is CocoaBlock) {
                if (state.get(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE) {
                    world.setBlockState(pos, state.with(CocoaBlock.AGE, 0), 2)
                    Block.dropStacks(state, world, pos, null, player, player!!.getStackInHand(hand))
                    info.returnValue = ActionResult.SUCCESS
                    return
                }
            }
        }
    }

}