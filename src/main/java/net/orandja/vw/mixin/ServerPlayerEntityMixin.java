package net.orandja.vw.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vw.maccessors.DoubleToolModeAccessor;
import net.orandja.vw.mods.DoubleToolMode;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

@SuppressWarnings("NullableProblems") @Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements DoubleToolModeAccessor {

    HashMap<Item, DoubleToolMode> toolModes = new HashMap<>();

    public DoubleToolMode getToolMode(ItemStack stack) {
        return toolModes.getOrDefault(stack.getItem(), DoubleToolMode.ALL);
    }

    public DoubleToolMode setToolMode(ItemStack stack, DoubleToolMode mode) {
        toolModes.put(stack.getItem(), mode);
        return mode;
    }

    public DoubleToolMode setNextToolMode(ItemStack stack, DoubleToolMode[] validModes) {
        return setToolMode(stack, getToolMode(stack).next(validModes));
    }

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }
}
