package net.orandja.vanillawaifu.mods;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.VanillaWaifu;
import net.orandja.vanillawaifu.utils.BlockUtils;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Related Mixins:
 * net.orandja.vanillawaifu.mixin.EnchantmentMixin
 */
public class EnchantBlocks {

    public static void init(MinecraftServer server) {
        //Brewing Stand
        VanillaWaifu.ACCEPTED_ENCHANTS.put(Items.BREWING_STAND, ImmutableList.of(Enchantments.BANE_OF_ARTHROPODS, Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.FIRE_ASPECT));
        VanillaWaifu.HOPPER_EXTRACT.add(EnchantBlocks::noExtractBrewingStand);

        //Furnace
        VanillaWaifu.ACCEPTED_ENCHANTS.put(Items.FURNACE, ImmutableList.of(Enchantments.SMITE, Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.FLAME, Enchantments.UNBREAKING, Enchantments.FIRE_ASPECT));

        //Hopper
        VanillaWaifu.ACCEPTED_ENCHANTS.put(Items.HOPPER, ImmutableList.of(Enchantments.EFFICIENCY, Enchantments.SILK_TOUCH));
    }

    public static boolean noExtractBrewingStand(World world, BlockPos pos) {
        return noExtractBrewingStand(world.getBlockEntity(pos));
    }

    public static boolean noExtractBrewingStand(BlockEntity blockEntity) {
        return blockEntity != null && BlockUtils.hasAccessor(blockEntity, "protected") && BlockUtils.<Boolean>access(blockEntity, "protected");
    }

    public static boolean canEnchant(ItemStack stack, Object enchantment) {
        return VanillaWaifu.ACCEPTED_ENCHANTS.containsKey(stack.getItem()) && (enchantment instanceof Enchantment) && VanillaWaifu.ACCEPTED_ENCHANTS.get(stack.getItem()).contains(enchantment);
    }

    public static void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (world instanceof ServerWorld) {
            ListTag enchantments = new ListTag();
            if (BlockUtils.<Boolean>access(blockEntity, "enchanted")) {
                BlockUtils.<Map<String, Short>>access(blockEntity, "Enchantments").forEach((id, lvl) -> {
                    if (lvl > 0) {
                        CompoundTag enchantment = new CompoundTag();
                        enchantment.putShort("lvl", lvl);
                        enchantment.putString("id", "minecraft:" + id);
                        enchantments.add(enchantment);
                    }
                });
            }

            BlockWithEntity.getDroppedStacks(state, (ServerWorld) world, pos, blockEntity, player, stack).forEach((itemStack) -> {
                if (!enchantments.isEmpty()) {
                    if (!itemStack.hasTag()) {
                        itemStack.setTag(new CompoundTag());
                    }
                    if (itemStack.getTag().contains("Enchantments")) {
                        ListTag listTag = (ListTag) itemStack.getTag().get("Enchantments");
                        enchantments.forEach(listTag::add);
                    } else {
                        itemStack.getTag().put("Enchantments", enchantments.copy());
                    }
                }
                BlockWithEntity.dropStack(world, pos, itemStack);
            });
            state.onStacksDropped((ServerWorld) world, pos, stack);
        }
    }

    public static void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!itemStack.hasTag() || !itemStack.getTag().contains("Enchantments")) {
            return;
        }

        ((ListTag)itemStack.getTag().get("Enchantments")).forEach(tag -> BlockUtils.notify(world, pos, "Enchantments", tag));
    }
}
