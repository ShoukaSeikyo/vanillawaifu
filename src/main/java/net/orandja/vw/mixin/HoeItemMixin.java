package net.orandja.vw.mixin;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.orandja.vw.logic.DoubleHoeLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;

@Mixin(HoeItem.class)
public abstract class HoeItemMixin implements DoubleHoeLogic {

    @Inject(cancellable = true, method = "useOnBlock", at = @At("HEAD"))
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        use(context, info);
//        World world = context.getWorld();
//        if (world.isClient) {
//            return;
//        }
//        BlockPos pos = context.getBlockPos();
//        PlayerEntity player = context.getPlayer();
//        Hand hand = context.getHand();
//        BlockState state = world.getBlockState(pos);
//        Block block = world.getBlockState(pos).getBlock();
//
//        if (block instanceof Fertilizable) {
//            if (block instanceof CropBlock cropBlock) {
//                if (cropBlock.isMature(state)) {
//                    if(hasTwoSuitableTools(player)) {
//                        Direction direction = Direction.getEntityFacingOrder(player)[0];
//                        int i = 0;
//                        BlockPos nPos;
//                        BlockState nState;
//                        List<BlockPos> toRemove = Lists.newArrayList();
//                        ItemStack mainTool = player.getMainHandStack();
//                        ItemStack offTool = player.getOffHandStack();
//
//                        while(i < 16 && (nState = world.getBlockState(nPos = pos.offset(direction, i))).getBlock() == block) {
//                            if(isGonnaBreak(mainTool) || isGonnaBreak(offTool)) {
//                                break;
//                            }
//                            if(cropBlock.isMature(nState)) {
//                                toRemove.add(nPos);
//                                player.getMainHandStack().damage(1, player, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));
//                                player.getOffHandStack().damage(1, player, (p) -> p.sendToolBreakStatus(Hand.OFF_HAND));
//                            }
//                            i++;
//                        }
//
//                        HashMap<Item, ItemStack> dropMap = new HashMap<>();
//
//                        toRemove.forEach(cropPos -> {
//                            world.setBlockState(cropPos, cropBlock.withAge(0), 2);
//                            Block.getDroppedStacks(state, (ServerWorld)world, pos, null, player, mainTool).forEach(stack -> {
//                                if(dropMap.containsKey(stack.getItem())) {
//                                    ItemStack otherStack = dropMap.get(stack.getItem());
//                                    int count = otherStack.getCount() + stack.getCount();
//                                    if(count > otherStack.getMaxCount()) {
//                                        otherStack.setCount(otherStack.getMaxCount());
//                                        Block.dropStack(world, pos, otherStack.copy());
//                                        count -= otherStack.getMaxCount();
//                                    }
//                                    stack.setCount(count);
//                                }
//
//                                dropMap.put(stack.getItem(), stack);
//                            });
//                        });
//                        ExperienceOrbEntity.spawn((ServerWorld) world, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), toRemove.size() - 1);
//
//                        dropMap.values().forEach(stack -> Block.dropStack(world, pos, stack));
//
//
//                        state.onStacksDropped((ServerWorld)world, pos, mainTool);
//
//                    } else {
//                        world.setBlockState(pos, cropBlock.withAge(0), 2);
//                        Block.dropStacks(state, world, pos, null, player, player.getStackInHand(hand));
//                        context.getStack().damage(1, player, (p) -> p.sendToolBreakStatus(context.getHand()));
//                    }
//                    info.setReturnValue(ActionResult.SUCCESS);
//                    return;
//                }
//            }
//
//            if (block instanceof CocoaBlock) {
//                if (state.get(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE) {
//                    world.setBlockState(pos, state.with(CocoaBlock.AGE, 0), 2);
//                    Block.dropStacks(state, world, pos, null, player, player.getStackInHand(hand));
//                    info.setReturnValue(ActionResult.SUCCESS);
//                    return;
//                }
//            }
//        }
    }

//
//
//    public boolean hasTwoSuitableTools(PlayerEntity player) {
//        if(player.getMainHandStack().getItem() instanceof HoeItem mainHand && player.getOffHandStack().getItem() instanceof HoeItem offHand) {
//            return mainHand.getMaterial() == offHand.getMaterial() && !(isGonnaBreak(player.getMainHandStack()) || isGonnaBreak(player.getOffHandStack()));
//        }
//        return false;
//    }
//
//    public boolean isGonnaBreak(ItemStack stack) {
//        return stack.isEmpty() || stack.getDamage() >= (stack.getMaxDamage() - 2);
//    }

}
