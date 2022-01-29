@file:Mixin(ChestBlock::class)
@file:JvmName("ChestBlockMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.AbstractChestBlock
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.Waterloggable
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.enums.ChestType
import net.minecraft.client.block.ChestAnimationProgress
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.orandja.vw.maccessors.ChestBlockEntityMixinAccessor
import net.orandja.vw.mods.WhitelistedChest
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Supplier

@Mixin(ChestBlock::class)
abstract class ChestBlockMixin(
    settings: Settings?,
    entityTypeSupplier: Supplier<BlockEntityType<out ChestBlockEntity>>?
) : AbstractChestBlock<ChestBlockEntity>(settings, entityTypeSupplier), Waterloggable {
    @Shadow
    abstract fun getNeighborChestDirection(ctx: ItemPlacementContext, dir: Direction): Direction

    @Inject(at = [At("HEAD")], method = ["onUse"], cancellable = true)
    fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult,
        info: CallbackInfoReturnable<ActionResult>
    ) {
        if (!WhitelistedChest.isWhitelisted(world = world, pos = pos, player = player)) {
            info.returnValue = ActionResult.PASS
        }
    }


    @Inject(at = [At("RETURN")], method = ["onPlaced"])
    fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity,
        stack: ItemStack,
        info: CallbackInfo
    ) {
        if (!stack.hasNbt() || !stack.nbt!!.contains("whitelist")) return

        val entity: ChestBlockEntity = world.getBlockEntity(pos) as? ChestBlockEntity ?: return
        val entityMixin: ChestBlockEntityMixinAccessor = entity as ChestBlockEntityMixinAccessor;
        stack.nbt!!.getList("whitelist", 8).map(NbtElement::asString).forEach(entityMixin.accessWhitelist()::add)
    }

    @Inject(method = ["getPlacementState"], at = [At("RETURN")], cancellable = true)
    open fun handleDoubleChest(ctx: ItemPlacementContext, info: CallbackInfoReturnable<BlockState>) {
        val blockState = info.returnValue
        val chestType = blockState.get(ChestBlock.CHEST_TYPE)
        if (chestType == ChestType.SINGLE) {
            return
        }
        var direction = blockState.get(ChestBlock.FACING)
        direction =
            if (chestType == ChestType.RIGHT) direction.rotateYCounterclockwise() else if (chestType == ChestType.LEFT) direction.rotateYClockwise() else direction
        val blockPos = ctx.blockPos.offset(direction)
        if (!(ctx.stack.hasNbt() && ctx.stack.nbt!!.contains("whitelist"))) {
            if (WhitelistedChest.hasWhitelist(world = ctx.world, pos = blockPos)) {
                (ctx.player as ServerPlayerEntity?)!!.networkHandler.sendPacket(
                    BlockUpdateS2CPacket(
                        ctx.world,
                        ctx.blockPos
                    )
                )
                (ctx.player as ServerPlayerEntity?)!!.networkHandler.sendPacket(
                    BlockUpdateS2CPacket(
                        ctx.world,
                        blockPos
                    )
                )
                info.setReturnValue(blockState.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE))
            }
            return
        }

        if (!WhitelistedChest.sameWhitelist(world = ctx.world, pos1 = blockPos, stackList = ctx.stack.nbt!!["whitelist"] as NbtList)) {
            (ctx.player as ServerPlayerEntity?)!!.networkHandler.sendPacket(
                BlockUpdateS2CPacket(
                    ctx.world,
                    ctx.blockPos
                )
            )
            (ctx.player as ServerPlayerEntity?)!!.networkHandler.sendPacket(BlockUpdateS2CPacket(ctx.world, blockPos))
            info.setReturnValue(blockState.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE))
            return
        }
    }
}

@Mixin(ChestBlockEntity::class)
abstract class ChestBlockEntityMixin(
    blockEntityType: BlockEntityType<*>,
    blockPos: BlockPos,
    blockState: BlockState
) : LootableContainerBlockEntity(
    blockEntityType, blockPos, blockState
), ChestAnimationProgress, ChestBlockEntityMixinAccessor {
    var whitelist: ArrayList<String> = ArrayList()

    override fun accessWhitelist(): ArrayList<String> {
        return whitelist;
    }

    @Inject(method = ["readNbt"], at = [At("RETURN")])
    fun readNbt(nbt: NbtCompound, info: CallbackInfo) {
        if (nbt.contains("whitelist")) nbt.getList("whitelist", 8).map(NbtElement::asString)
            .forEach { uuid ->
                this.whitelist.add(uuid)
            }
    }

    @Inject(method = ["writeNbt"], at = [At("RETURN")])
    fun writeNbt(nbt: NbtCompound, info: CallbackInfoReturnable<NbtCompound>) {
        if (whitelist.size > 0) {
            nbt.put("whitelist", NbtList().apply { whitelist.map(NbtString::of).forEach(this::add) })
        }
    }
}