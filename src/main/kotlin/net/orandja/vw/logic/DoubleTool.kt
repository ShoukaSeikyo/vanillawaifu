package net.orandja.vw.logic

import com.google.common.collect.Lists
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.orandja.mcutils.*
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer


interface DoubleTool {

    fun dropStacks(
        blockEntity: BlockEntity?,
        world: World,
        player: PlayerEntity,
        pos: BlockPos,
        state: BlockState,
        stack: ItemStack,
        count: Int) {
        if (world is ServerWorld) {
            Block.getDroppedStacks(state, world, pos, blockEntity, player, stack).forEach {
                it.increment(count)
                Block.dropStack(world, pos, it)
            }
            state.onStacksDropped(world, pos, stack)
        }
    }

    fun useDoubleAxe(
        block: Block,
        blockEntity: BlockEntity?,
        world: World,
        player: PlayerEntity,
        pos: BlockPos,
        state: BlockState,
        stack: ItemStack
    ): Boolean {
        if (!world.isClient && state.isWood() && player.areBothToolsSuitable(
                state,
                AxeItem::class.java
            ) && !player.anyToolBreaking()
        ) {
            player.incrementStat(Stats.MINED.getOrCreateStat(block))
            player.addExhaustion(0.005f)

            var count = 0
            DoubleToolMode.AXE.task(world, pos, player, state, player.getBothTools()) {
                player.mainHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
                player.offHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND) }

                player.incrementStat(Stats.MINED.getOrCreateStat(state.block))
                player.addExhaustion(0.005f)
                world.setBlockState(it, Blocks.AIR.defaultState, 2)
                count++

                if(count >= 64) {
                    dropStacks(blockEntity, world, player, pos, state, stack, count)
                    count = 0
                }
            }

            dropStacks(blockEntity, world, player, pos, state, stack, count)
            return false
        }

        // Executes Default afterBreak
        return true
    }

    fun useDoubleHoe(context: ItemUsageContext, info: CallbackInfoReturnable<ActionResult>) {
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
            if (block is CropBlock && block.isMature(state)) {
                if (!player.areBothTools(HoeItem::class.java)) {
                    world.setBlockState(pos, block.withAge(0), 2)
                    Block.dropStacks(state, world, pos, null, player, player.getStackInHand(hand))
                    context.stack.damage(1, player) { p: PlayerEntity -> p.sendToolBreakStatus(context.hand) }

                    info.returnValue = ActionResult.SUCCESS
                    return
                }

                val direction = Direction.getEntityFacingOrder(player)[0]
                var i = 0
                var nPos: BlockPos? = null
                var nState: BlockState? = null
                val toRemove: MutableList<BlockPos> = Lists.newArrayList()
                val mainTool = player.mainHandStack
//                val offTool = player.offHandStack
                while (i < 16 && world.getBlockState(pos.offset(direction, i).also { nPos = it })
                        .also { nState = it }.block === block
                ) {
                    if (player.anyToolBreaking()) {
                        break
                    }
                    if (block.isMature(nState!!)) {
                        toRemove.add(nPos!!)
                        player.mainHandStack.damage(
                            1,
                            player
                        ) { p: PlayerEntity -> p.sendToolBreakStatus(Hand.MAIN_HAND) }
                        player.offHandStack.damage(
                            1,
                            player
                        ) { p: PlayerEntity -> p.sendToolBreakStatus(Hand.OFF_HAND) }
                    }
                    i++
                }
                val dropMap = HashMap<Item, ItemStack>()
                toRemove.forEach(Consumer { cropPos: BlockPos? ->
                    world.setBlockState(cropPos, block.withAge(0), 2)
                    Block.getDroppedStacks(state, world as ServerWorld, pos, null, player, mainTool)
                        .forEach(Consumer { stack: ItemStack ->
                            if (dropMap.containsKey(stack.item)) {
                                val otherStack = dropMap[stack.item]
                                var count = otherStack!!.count + stack.count
                                if (count > otherStack.maxCount) {
                                    otherStack.count = otherStack.maxCount
                                    Block.dropStack(world, pos, otherStack.copy())
                                    count -= otherStack.maxCount
                                }
                                stack.count = count
                            }
                            dropMap[stack.item] = stack
                        })
                })
                ExperienceOrbEntity.spawn(
                    world as ServerWorld,
                    Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()),
                    toRemove.size - 1
                )
                dropMap.values.forEach(Consumer { stack: ItemStack? -> Block.dropStack(world, pos, stack) })
                state.onStacksDropped(world, pos, mainTool)

                info.returnValue = ActionResult.SUCCESS
                return
            }


            if (block is CocoaBlock) {
                if (state.get(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE) {
                    world.setBlockState(pos, state.with(CocoaBlock.AGE, 0), 2)
                    Block.dropStacks(state, world, pos, null, player, player.getStackInHand(hand))
                    info.returnValue = ActionResult.SUCCESS
                    return
                }
            }
        }
    }

    fun useDoublePickaxe(
        value: Boolean,
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos,
        player: LivingEntity,
        clazz: Class<*>
    ): Boolean {
        if (!world.isClient && player is PlayerEntity && player.areBothToolsSuitable(state, clazz)) {
            player.getToolMode().task(world, pos, player, state, player.getBothTools()) {
                player.mainHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
                player.offHandStack.damage(1, player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND) }

                player.incrementStat(Stats.MINED.getOrCreateStat(state.block))
                player.addExhaustion(0.005f)
                player.tryBreakBlock(it)
            }
        }

        return value
    }

    fun useDoubleShovel(value: Boolean, stack: ItemStack, world: World, state: BlockState, pos: BlockPos, player: LivingEntity, clazz: Class<*>): Boolean = useDoublePickaxe(value, stack, world, state, pos, player, clazz)

    var toolModes: HashMap<Item, DoubleToolMode>

    fun getToolMode(stack: ItemStack): DoubleToolMode {
        return toolModes.getOrDefault(stack.item, DoubleToolMode.ALL)
    }

    fun setToolMode(stack: ItemStack, mode: DoubleToolMode): DoubleToolMode {
        toolModes.put(stack.item, mode)
        return mode
    }

    fun setNextToolMode(stack: ItemStack, validModes: Array<DoubleToolMode>): DoubleToolMode? {
        return setToolMode(stack, getToolMode(stack).next(validModes))
    }

    companion object {
        val AXE_ADJASCENTS_ZONE = BlockZone(-1..1, -1..1, 0..1)
        val PICKAXE_ADJASCENTS_ZONE = BlockZone(-1..1, -1..1, -1..1)
        val DIRECTION_ADJASCENTS_ZONE = mapOf(
            Direction.EAST to BlockZone(0..0, -1..1, -1..1),
            Direction.WEST to BlockZone(0..0, -1..1, -1..1),
            Direction.NORTH to BlockZone(-1..1, 0..0, -1..1),
            Direction.SOUTH to BlockZone(-1..1, 0..0, -1..1),
            Direction.UP to BlockZone(-1..1, -1..1, 0..0),
            Direction.DOWN to BlockZone(-1..1, -1..1, 0..0)
        )

        fun beforeLaunch() {
            val pickaxeValidModes = arrayOf(DoubleToolMode.ALL, DoubleToolMode.SIMILAR, DoubleToolMode.VEIN)
            InventoryMiddleClick.onMiddleClick.add(
                { stack: ItemStack -> stack.item is PickaxeItem } to { stack, player -> (player as? DoubleTool)?.setNextToolMode(stack, pickaxeValidModes)?.sendToPlayer(player) }
            )

            val shovelValidModes = arrayOf(DoubleToolMode.ALL, DoubleToolMode.SIMILAR)
            InventoryMiddleClick.onMiddleClick.add(
                { stack: ItemStack -> stack.item is ShovelItem } to { stack, player -> (player as? DoubleTool)?.setNextToolMode(stack, shovelValidModes)?.sendToPlayer(player) }
            )
        }
    }
}

enum class DoubleToolMode(
    val value: Int,
    val task: ((World, BlockPos, PlayerEntity, BlockState, Pair<ItemStack, ItemStack>, ((BlockPos) -> Unit)) -> Unit)
) {
    SIMILAR(value = 0,
        task = { world, pos, player, state, hands, task ->
            DoubleTool.DIRECTION_ADJASCENTS_ZONE[Direction.getEntityFacingOrder(player)[0]]?.get(world, pos, repeat = { false to null}, validate = { state.block == it.block })?.forEach {
                if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                    task(it)
                }
            }
        }),
    ALL(1,
        task = { world, pos, player, _, hands, task ->
            DoubleTool.DIRECTION_ADJASCENTS_ZONE[Direction.getEntityFacingOrder(player)[0]]?.get(world, pos, repeat = { false to null }, validate = { hands.first.isSuitableFor(it) })?.forEach {
                if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                    task(it)
                }
            }
        }),
    VEIN(2,
        task = { world, pos, _, state, hands, task ->
            if(state.isOre()) {
                DoubleTool.PICKAXE_ADJASCENTS_ZONE.get(world, pos, repeat = {
                    world.getBlockState(it).isOre(state) to it
                }, validate =  { it.isOre(state) }).forEach {
                    if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                        task(it)
                    }
                }
            }
        }),
    AXE(3,
        task = { world, pos, _, state, hands, task ->
            if(state.isWood()) {
                DoubleTool.AXE_ADJASCENTS_ZONE.get(
                    world,
                    pos,
                    iterateMax = 128,
                    repeat = {
                        world.getBlockState(it).isWood(state) to it
                    },
                    validate = { it.isWood(state) }
                ).forEach {
                    if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                        task(it)
                    }
                }
            }
        });

    companion object {
        fun next(value: Int): DoubleToolMode {
            return values()[(value + 1) % values().size]
        }
    }

    fun next(): DoubleToolMode {
        return next(this.value)
    }

    fun next(validModes: Array<DoubleToolMode>): DoubleToolMode {
        return validModes[(validModes.indexOf(this) + 1) % validModes.size]
    }

    fun sendToPlayer(player: PlayerEntity) {
        player.sendMessage(LiteralText("Set tool mode to: $name"), true)
    }
}

class BlockZone(private val xRange: IntRange, private val zRange: IntRange, private val yRange: IntRange) {

    private fun iterate(iterator: (x: Int, y: Int, z: Int) -> Unit) {
        yRange.forEach { y ->
            xRange.forEach { x ->
                zRange.forEach { z ->
                    iterator(x, y, z)
                }
            }
        }
    }

    fun get(
        world: World,
        origin: BlockPos,
        iterateMax: Int = 32,
        iterate: Int = 0,
        repeat: (origin: BlockPos) -> Pair<Boolean, BlockPos?> = { false to null },
        validated: MutableMap<BlockPos, Boolean> = mutableMapOf(),
        validate: (BlockState) -> Boolean
    ): List<BlockPos> {
        iterate { x, y, z ->
            val pos = origin.add(x, y, z)
            if (!validated.containsKey(pos)) {
                validated[pos] = validate(world.getBlockState(pos))
                val willRepeat = repeat(pos)
                if (willRepeat.first && iterate < iterateMax) {
                    get(world, willRepeat.second!!, iterateMax, iterate + 1, repeat, validated, validate)
                }
            }
        }

        return validated.filter { it.value }.map { it.key }
    }

}