package net.orandja.vw.mods

import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ShovelItem
import net.minecraft.item.ToolItem
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.orandja.vw.crafting.DoubleToolModeRecipe
import net.orandja.vw.maccessors.DoubleToolModeAccessor
import net.orandja.vw.utils.VWRecipes

operator fun BlockPos.plus(pos: Array<Int>): BlockPos {
    return this.add(pos[0], pos[1], pos[2])
}

fun ItemStack.isGonnaBreak(): Boolean {
    return isEmpty || damage >= maxDamage - 2
}

fun PlayerEntity.anyToolBreaking(): Boolean {
    return this.mainHandStack.isGonnaBreak() || this.offHandStack.isGonnaBreak()
}

fun Block.isOre(): Boolean {
    return this is OreBlock || this is RedstoneOreBlock
}

fun Block.isOre(block: Block): Boolean {
    return this.isOre() && block == this
}

fun BlockState.isOre(): Boolean {
    return block.isOre()
}

fun BlockState.isOre(block: Block): Boolean {
    return this.block.isOre(block)
}

fun BlockState.isOre(state: BlockState): Boolean {
    return this.isOre(state.block)
}

fun BlockState.isWood(): Boolean {
    return isIn(BlockTags.LOGS) || material == Material.NETHER_WOOD
}

fun BlockState.isWood(state: BlockState): Boolean {
    return this.isWood() && state.block == this.block
}

enum class DoubleToolMode(
    val value: Int,
    val task: ((World, BlockPos, PlayerEntity, BlockState, Pair<ItemStack, ItemStack>, ((BlockPos) -> Unit)) -> Unit)
) {
    SIMILAR(value = 0,
        task = { world, pos, player, state, hands, task ->
            DoubleTools.DIRECTION_ADJASCENTS_ZONE[Direction.getEntityFacingOrder(player)[0]]?.get(world, pos, repeat = { false to null}, validate = { state.block == it.block })?.forEach {
                if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                    task(it)
                }
            }
        }),
    ALL(1,
        task = { world, pos, player, state, hands, task ->
            DoubleTools.DIRECTION_ADJASCENTS_ZONE[Direction.getEntityFacingOrder(player)[0]]?.get(world, pos, repeat = { false to null }, validate = { hands.first.isSuitableFor(it) })?.forEach {
                if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                    task(it)
                }
            }
        }),
    VEIN(2,
        task = { world, pos, player, state, hands, task ->
            if(state.isOre()) {
                DoubleTools.PICKAXE_ADJASCENTS_ZONE.get(world, pos, repeat = {
                    world.getBlockState(it).isOre(state) to it
                }, validate =  { it.isOre(state) }).forEach {
                    if (!hands.first.isGonnaBreak() && !hands.second.isGonnaBreak()) {
                        task(it)
                    }
                }
            }
        }),
    AXE(3,
        task = { world, pos, player, state, hands, task ->
            if(state.isWood()) {
                DoubleTools.AXE_ADJASCENTS_ZONE.get(
                    world,
                    pos,
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
        iterateMax: Int = 16,
        iterate: Int = 0,
        repeat: (origin: BlockPos) -> Pair<Boolean, BlockPos?> = { false to null },
        validated: MutableMap<BlockPos, Boolean> = mutableMapOf(),
        validate: (BlockState) -> Boolean
    ): List<BlockPos> {
        iterate { x, y, z ->
            val pos = origin.add(x, y, z);
            if (!validated.containsKey(pos)) {
                validated[pos] = validate(world.getBlockState(pos))
                val willRepeat = repeat(pos);
                if (willRepeat.first && iterate < iterateMax) {
                    get(world, willRepeat.second!!, iterateMax, iterate + 1, repeat, validated, validate)
                }
            }
        }

        return validated.filter { it.value }.map { it.key }
    }

}

class DoubleTools {

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

        fun launch() {
            VWRecipes.addRecipe(
                DoubleToolModeRecipe(
                    Identifier("vanillawaifu", "pickaxe_toolmode"),
                    arrayOf(DoubleToolMode.ALL, DoubleToolMode.SIMILAR, DoubleToolMode.VEIN),
                    PickaxeItem::class.java
                )
            )
            VWRecipes.addRecipe(
                DoubleToolModeRecipe(
                    Identifier("vanillawaifu", "shovel_toolmode"),
                    arrayOf(DoubleToolMode.ALL, DoubleToolMode.SIMILAR),
                    ShovelItem::class.java
                )
            )
        }

        fun areBothSuitable(player: PlayerEntity, state: BlockState, clazz: Class<*>): Boolean {
            if (clazz.isInstance(player.mainHandStack.item) && clazz.isInstance(player.offHandStack.item)) {
                return (player.mainHandStack.item as ToolItem).material == (player.offHandStack.item as ToolItem).material && player.mainHandStack.isSuitableFor(
                    state
                )
            }
            return false
        }

        fun PlayerEntity.areBothToolsSuitable(state: BlockState, clazz: Class<*>): Boolean {
            if (clazz.isInstance(mainHandStack.item) && clazz.isInstance(offHandStack.item)) {
                return (mainHandStack.item as ToolItem).material == (offHandStack.item as ToolItem).material && mainHandStack.isSuitableFor(
                    state
                )
            }
            return false
        }

        fun areBothTools(player: PlayerEntity, clazz: Class<*>): Boolean {
            if (clazz.isInstance(player.mainHandStack.item) && clazz.isInstance(player.offHandStack.item)) {
                return (player.mainHandStack.item as ToolItem).material == (player.offHandStack.item as ToolItem).material
            }
            return false
        }

        fun PlayerEntity.getToolMode(): DoubleToolMode {
            return (this as DoubleToolModeAccessor).getToolMode(this.mainHandStack)
        }

        fun PlayerEntity.getBothTools(): Pair<ItemStack, ItemStack> {
            return Pair(mainHandStack, offHandStack)
        }

        fun PlayerEntity.tryBreakBlock(pos: BlockPos) {
            val blockEntity = world.getBlockEntity(pos)
            val state = world.getBlockState(pos)
            val block = state.block
            if (world.breakBlock(pos, false)) {
                block.onBroken(world, pos, state)
                if (canHarvest(state)) {
                    block.afterBreak(world, this, pos, state, blockEntity, mainHandStack.copy())
                }
            }
        }
    }

}