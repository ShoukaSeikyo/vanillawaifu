package net.orandja.vanillawaifu.mods;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.orandja.vanillawaifu.VanillaWaifu;
import net.orandja.vanillawaifu.crafting.ModifierShapedRecipe;
import net.orandja.vanillawaifu.utils.BlockUtils;
import net.orandja.vanillawaifu.utils.RecipeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.orandja.vanillawaifu.utils.RecipeUtils.createObject;

/**
 * Related Mixins:
 * net.orandja.vanillawaifu.mixin.ChestBlockEntityMixin
 * net.orandja.vanillawaifu.mixin.ChestBlockMixin
 * */
public class WhitelistedChest {

    public static void init(MinecraftServer server) {
        createObject(new ItemStack(Items.CHEST), stack -> {
            Map<String, Ingredient> map = new HashMap<String, Ingredient>() {{
                put("C", Ingredient.fromJson(new JsonParser().parse("{\"item\":\"minecraft:chest\"}")));
            }};

            RecipeUtils.addRecipe(new ModifierShapedRecipe(new Identifier("vanillawaifu", "whitelisted_chest"), "", 1, 1, RecipeUtils.getIngredients(new String[]{"C"}, map, 1, 1), stack)
                    .setConsumer(WhitelistedChest::handleChestCraft));
        });

        VanillaWaifu.PROTECTORS.add(WhitelistedChest::canDestroy);
        VanillaWaifu.HOPPER_EXTRACT.add(WhitelistedChest::hasWhitelist);
    }

    public static ItemStack handleChestCraft(CraftingInventory inventory, PlayerEntity player, ItemStack normalOutput) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack craftingStack = inventory.getStack(i);
            if (!craftingStack.isEmpty() && craftingStack.getItem() == Items.CHEST) {
                ItemStack itemStack = normalOutput;
                normalOutput = craftingStack.copy();
                normalOutput.setCount(itemStack.getCount());
                break;
            }
        }

        normalOutput.setTag(createObject(normalOutput.hasTag() ? normalOutput.getTag() : new CompoundTag(), tag -> {
            tag.put("whitelist", createObject(tag.contains("whitelist") ? tag.getList("whitelist", 8) : new ListTag(), whiteListTag -> {
                String playerUUID = player.getUuidAsString();
                for (Tag whitelisted : whiteListTag) {
                    if (whitelisted.asString().equals(playerUUID)) {
                        return;
                    }
                }

                whiteListTag.add(StringTag.of(playerUUID));
                tag.put("display", createObject(tag.contains("display") ? (CompoundTag) tag.get("display") : new CompoundTag(), displayTag -> {
                    if (displayTag.contains("Lore")) {
                        displayTag.getList("Lore", 8).forEach(loreTag -> System.out.println(loreTag.toString()));
                        displayTag.getList("Lore", 8).add(StringTag.of("{\"text\":\"— " + player.getName().asString() + "\",\"color\":\"green\"}"));
                        return;
                    }

                    displayTag.put("Lore", createObject(new ListTag(), tagList -> {
                        tagList.add(StringTag.of("{\"text\":\"Whitelisted Chest allowed for: \",\"color\":\"green\"}"));
                        tagList.add(StringTag.of("{\"text\":\"— " + player.getName().asString() + "\",\"color\":\"green\"}"));
                    }));
                }));
            }));
        }));
        return normalOutput;
    }

    public static boolean compareWhitelists(World world, BlockPos pos, ListTag chest2) {
        if(hasWhitelist(world, pos)) {
            List<String> whitelist = BlockUtils.access(world, pos, "whitelist");
            return chest2.stream().map(Tag::asString).filter(whitelist::contains).count() == whitelist.size();
        }

        return false;
    }

    public static boolean isWhitelisted(BlockEntity blockEntity, Object player) {
        if(blockEntity == null || !(player instanceof PlayerEntity) || !hasWhitelist(blockEntity)) {
            return true;
        }

        return ((PlayerEntity) player).isCreative() || BlockUtils.<List>access(blockEntity, "whitelist").contains(((PlayerEntity) player).getUuidAsString());
    }

    public static boolean isWhitelisted(World world, BlockPos pos, Object player) {
        return isWhitelisted(world.getBlockEntity(pos), player);
    }

    public static boolean hasWhitelist(BlockEntity blockEntity) {
        return blockEntity != null && BlockUtils.hasAccessor(blockEntity, "whitelist") && BlockUtils.<List>access(blockEntity, "whitelist").size() > 0;
    }

    public static boolean hasWhitelist(World world, BlockPos pos) {
        return hasWhitelist(world.getBlockEntity(pos));
    }

    public static boolean canDestroy(World world, BlockPos pos, Object entity) {
        return !hasWhitelist(world, pos) || ((entity instanceof PlayerEntity) && isWhitelisted(world, pos, entity));
    }
}
