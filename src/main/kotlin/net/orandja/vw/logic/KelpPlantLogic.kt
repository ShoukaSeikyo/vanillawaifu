//package net.orandja.vw.logic
//
//import net.minecraft.block.Block
//import net.minecraft.block.BlockState
//import net.minecraft.block.Blocks
//import net.minecraft.block.entity.BlockEntity
//import net.minecraft.entity.player.PlayerEntity
//import net.minecraft.item.ItemStack
//import net.minecraft.server.world.ServerWorld
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import net.minecraft.world.World
//
//interface KelpPlantLogic {
//
//    fun getKelpPlant(): Block
//
//    fun afterBreak(block: Block, world: World, player: PlayerEntity?, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, stack: ItemStack) {
//        if (world is ServerWorld) {
//            var upperPos: BlockPos = pos
//
//            Block.getDroppedStacks(state, world, pos, blockEntity, player, stack).forEach { itemStack ->
//                while (isKelp(world, upperPos.offset(Direction.Axis.Y, 1).also { upperPos = it })) {
//                    itemStack.increment(1)
//                    world.setBlockState(upperPos, Blocks.WATER.defaultState, 2)
//                }
//                Block.dropStack(world, pos, itemStack)
//            }
//            state.onStacksDropped(world, pos, stack, true)
//        }
//    }
//
//    fun isKelp(world: World, pos: BlockPos): Boolean {
//        val state = world.getBlockState(pos)
//        return state.isOf(getKelpPlant()) || state.isOf(Blocks.KELP)
//    }
//}