package net.orandja.vanillawaifu.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.mods.WhitelistedChest;
import net.orandja.vanillawaifu.utils.BlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin extends AbstractChestBlock<ChestBlockEntity> implements Waterloggable {

    protected ChestBlockMixin(AbstractBlock.Settings settings, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityTypeSupplier) {
        super(settings, blockEntityTypeSupplier);
    }

    @Shadow
    abstract Direction getNeighborChestDirection(ItemPlacementContext ctx, Direction dir);

    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    public void handleDoubleChest(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
        BlockState blockState = info.getReturnValue();
        ChestType chestType = blockState.get(ChestBlock.CHEST_TYPE);
        if (chestType == ChestType.SINGLE) {
            return;
        }

        Direction direction = blockState.get(ChestBlock.FACING);
        direction = chestType == ChestType.RIGHT ? direction.rotateYCounterclockwise() : chestType == ChestType.LEFT ? direction.rotateYClockwise() : direction;
        BlockPos blockPos = ctx.getBlockPos().offset(direction);

        if (!(ctx.getStack().hasTag() && ctx.getStack().getTag().contains("whitelist"))) {
            if (WhitelistedChest.hasWhitelist(ctx.getWorld(), blockPos)) {
                ((ServerPlayerEntity) ctx.getPlayer()).networkHandler.sendPacket(new BlockUpdateS2CPacket(ctx.getWorld(), ctx.getBlockPos()));
                ((ServerPlayerEntity) ctx.getPlayer()).networkHandler.sendPacket(new BlockUpdateS2CPacket(ctx.getWorld(), blockPos));
                info.setReturnValue(blockState.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE));
            }
            return;
        }

        if (!WhitelistedChest.compareWhitelists(ctx.getWorld(), blockPos, (ListTag) ctx.getStack().getTag().get("whitelist"))) {
            ((ServerPlayerEntity) ctx.getPlayer()).networkHandler.sendPacket(new BlockUpdateS2CPacket(ctx.getWorld(), ctx.getBlockPos()));
            ((ServerPlayerEntity) ctx.getPlayer()).networkHandler.sendPacket(new BlockUpdateS2CPacket(ctx.getWorld(), blockPos));
            info.setReturnValue(blockState.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE));
            return;
        }
    }


    @Inject(at = @At("RETURN"), method = "onPlaced")
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo info) {
        if (!itemStack.hasTag() || !itemStack.getTag().contains("whitelist"))
            return;

        itemStack.getTag().getList("whitelist", 8).stream().map(Tag::asString).forEach(uuid -> BlockUtils.notify(world, pos, "whitelist", uuid));
        //BlockUtils.getBlockEntityRegistry(world, pos).put("whitelist", itemStack.getTag().getList("whitelist", 8).stream().map(Tag::asString).collect(Collectors.toList()));
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        if (!WhitelistedChest.isWhitelisted(world, pos, player)) {
            info.setReturnValue(ActionResult.PASS);
        }
    }
}
