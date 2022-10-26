package net.orandja.vw.mods.WhitelistedChestBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.mcutils.InventoryUtils;
import net.orandja.mcutils.NBTUtils;
import net.orandja.vw.VW;
import net.orandja.vw.logic.CustomRecipe;
import net.orandja.vw.mods.ProtectBlock.ProtectBlock;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface WhitelistedChestBlock {

    class WhitelistedChestRecipe extends ShapelessRecipe {

        public WhitelistedChestRecipe(Identifier id, String group, ItemStack output, DefaultedList<Ingredient> ingredients) {
            super(id, group, output, ingredients);
        }

        @Override
        public boolean matches(CraftingInventory craftingInventory, World world) {
            return InventoryUtils.hasListeners(craftingInventory) && InventoryUtils.getPlayer(craftingInventory) != null && super.matches(craftingInventory, world);
        }

        @Override
        public ItemStack craft(CraftingInventory craftingInventory) {
            PlayerEntity player = InventoryUtils.getPlayer(craftingInventory);
            if(player != null) {
                ItemStack chestOutput = getOutput().copy();

                NbtCompound tag = chestOutput.getOrCreateNbt();

                NbtList whitelist = NBTUtils.getOrCreate(tag, "whitelist", (key) -> tag.getList(key, NbtElement.STRING_TYPE), NbtList::new);
                Optional<String> first = whitelist.stream().map(NbtElement::asString).filter(player.getUuidAsString()::equals).findFirst();

                if(first.isPresent()) {
                    String uuid = first.get();
                    whitelist.add(NbtString.of(uuid));


                    NbtCompound display = NBTUtils.getOrCreate(tag, "display", tag::getCompound, NbtCompound::new);
                    NbtList lore = NBTUtils.getOrCreate(display, "Lore", key -> display.getList(key, NbtElement.STRING_TYPE), () -> {
                        NbtList list = new NbtList();
                            list.add(NbtString.of("{\"text\":\"Whitelisted Chest allowed for: \",\"color\":\"green\"}"));
                        return list;
                    });
                    lore.add(NbtString.of("{\"text\":\"— "+ player.getName().getString() +"\",\"color\":\"green\"}"));
                }

                return chestOutput;
            }
            return ItemStack.EMPTY;
        }
    }

    static void addNbt(List<String> list, NbtCompound nbt) {
        if(nbt.contains("whitelist")) {
            nbt.getList("whitelist", NbtElement.STRING_TYPE).stream().map(NbtElement::asString).forEach(list::add);
        }
    }

    static void toNBT(List<String> list, NbtCompound nbt) {
        if(list.size() > 0) {
            NbtList nbtList = new NbtList();
            list.stream().map(NbtString::of).forEach(nbtList::add);
            nbt.put("whitelist", nbtList);
        }
    }

    static String UUIDToUsername(String uuid) {
        //noinspection OptionalGetWithoutIsPresent
        return VW.Companion.getServerReference().get().getUserCache().getByUuid(UUID.fromString(uuid)).get().getName();
    }

    static boolean hasWhitelist(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasNbt() && stack.getNbt().contains("whitelist");
    }

    static void sendBlockUpdate(ServerPlayerEntity player, World world, BlockPos... pos) {
        for (BlockPos p : pos) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, p));
        }
    }

    static BlockPos getAdjascentPos(BlockState state, BlockPos pos) {
        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
        if(chestType == ChestType.SINGLE) {
            return null;
        }

        return switch (chestType) {
            case LEFT -> pos.offset(state.get(ChestBlock.FACING).rotateYClockwise());
            case RIGHT -> pos.offset(state.get(ChestBlock.FACING).rotateYCounterclockwise());
            default -> pos.offset(state.get(ChestBlock.FACING));
        };
    }

    static WhitelistedChestBlock getChest(World world, BlockPos pos) {
        try {
            return !world.isClient ? ((WhitelistedChestBlock)world.getBlockEntity(pos)) : null;
        } catch(Exception e) {
            return null;
        }
    }

    static void beforeLaunch() {
        CustomRecipe.Companion.getCustomShapelessRecipes().put(new Identifier("vanillawaifu", "whitelisted_chest"), WhitelistedChestRecipe::new);
        ProtectBlock.DESTROY.add((world, pos, entity) -> {
           WhitelistedChestBlock chest = getChest(world, pos);
           if(chest == null) {
               return true;
           }

           return chest.canDestroy(entity);
        });
        ProtectBlock.EXPLOSION_PROTECTION.add((world, pos) -> {
            WhitelistedChestBlock chest = getChest(world, pos);
            if(chest == null) {
                return false;
            }

            return !chest.hasNoWhitelist();
        });


        ProtectBlock.EXTRACTION_PREVENTION.add((world, pos) -> {
            WhitelistedChestBlock chest = getChest(world, pos);
            if(chest == null) {
                return true;
            }

            return chest.hasWhitelist();
        });
    }

    List<String> getWhitelist();

    default void loadWhitelist(NbtCompound nbt) {
        addNbt(getWhitelist(), nbt);
    }

    default void saveWhitelist(NbtCompound nbt) {
        toNBT(getWhitelist(), nbt);
    }

    default boolean hasWhitelist() {
        return getWhitelist().size() > 0;
    }

    default boolean hasNoWhitelist() {
        return getWhitelist().size() == 0;
    }

    default boolean canDestroy(Entity entity) {
        return hasNoWhitelist() || isWhitelisted(entity);
    }

    default boolean isWhitelisted(Entity entity) {
        if(entity instanceof PlayerEntity player) {
            return isWhitelisted(player);
        } else {
            return false;
        }
    }

    default boolean isWhitelisted(PlayerEntity player) {
        return hasNoWhitelist() || player.isCreative() || getWhitelist().contains(player.getUuidAsString());
    }

    default boolean compareStack(ItemStack stack) {
        //noinspection ConstantConditions
        if(stack.hasNbt() && stack.getNbt().contains("whitelist")) {
            return stack.getNbt().getList("whitelist", NbtElement.STRING_TYPE).stream().map(NbtElement::asString).filter(getWhitelist()::contains).count() == getWhitelist().size();

        }

        return false;
    }

    default void notifyPlayer(PlayerEntity player, CallbackInfoReturnable<ActionResult> info) {
        if(!isWhitelisted(player)) {
            String players = getWhitelist().stream().map(WhitelistedChestBlock::UUIDToUsername).collect(Collectors.joining(","));
            player.sendMessage(Text.of("[{\"text\":\"Whitelisted for: \",\"color\":\"red\"},{\"text\":\""+ players +"\",\"color\":\"green\"}]"), true);
            info.setReturnValue(ActionResult.SUCCESS);
        }
    }

    default void handleStack(ItemStack stack) {
        //noinspection ConstantConditions
        if(stack.hasNbt() && stack.getNbt().contains("whitelist")) {
            stack.getNbt().getList("whitelist", NbtElement.STRING_TYPE).stream().map(NbtElement::asString).forEach(getWhitelist()::add);
        }
    }

    default void onBlockUse(World world, BlockPos pos, PlayerEntity player, CallbackInfoReturnable<ActionResult> info) {
        WhitelistedChestBlock chest = getChest(world, pos);
        if(chest != null) {
            chest.notifyPlayer(player, info);
        }
    }

    default void onBlockPlaced(World world, BlockPos pos, ItemStack stack) {
        WhitelistedChestBlock chest = getChest(world, pos);
        if(chest != null) {
            chest.handleStack(stack);
        }
    }

    default void onPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
        if(ctx.getWorld().isClient) {
            return;
        }

        BlockPos adjacentPos = getAdjascentPos(info.getReturnValue(), ctx.getBlockPos());
        if(adjacentPos == null) {
            return;
        }
        WhitelistedChestBlock adjacentChest = getChest(ctx.getWorld(), adjacentPos);
        if(adjacentChest == null) {
            return;
        }

        if((hasWhitelist(ctx.getStack()) && adjacentChest.compareStack(ctx.getStack())) || !adjacentChest.hasWhitelist()) {
            return;
        }

        if(ctx.getPlayer() instanceof ServerPlayerEntity player) {
            sendBlockUpdate(player, ctx.getWorld(), ctx.getBlockPos(), adjacentPos);
        }
        info.setReturnValue(info.getReturnValue().with(ChestBlock.CHEST_TYPE, ChestType.SINGLE));
    }
}
