//package net.orandja.vw.crafting;
//
//import net.minecraft.enchantment.Enchantment;
//import net.minecraft.enchantment.EnchantmentHelper;
//import net.minecraft.entity.ExperienceOrbEntity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.inventory.Inventory;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.screen.ScreenHandlerContext;
//import net.minecraft.screen.slot.Slot;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//
//import java.util.Map;
//import java.util.function.BiConsumer;
//
//
//public class OutputSlot extends Slot {
//
//        Inventory input;
//        Inventory result;
//        ScreenHandlerContext context;
//
//        public OutputSlot(Inventory input, Inventory result, ScreenHandlerContext context) {
//            super(result, 2, 129, 34);
//            this.input = input;
//            this.result = result;
//            this.context = context;
//        }
//
//        public boolean canInsert(ItemStack stack) {
//            return false;
//        }
//
//        public void onTakeItem(PlayerEntity player, ItemStack stack) {
//            if (getBookAndTool((book, tool, bookSlot, toolSlot) -> {
//                input.setStack(bookSlot, ItemStack.EMPTY);
//
//                ItemStack toolStack = input.getStack(toolSlot);
//                toolStack.removeSubNbt("Enchantments");
//                toolStack.removeSubNbt("StoredEnchantments");
//                input.setStack(toolSlot, toolStack);
//            })) {
//                return;
//            }
//
//            context.run((world, pos) -> {
//                if (world instanceof ServerWorld) {
//                    ExperienceOrbEntity.spawn((ServerWorld) world, Vec3d.ofCenter(pos), this.getExperience(world));
//                }
//
//                world.syncWorldEvent(1042, pos, 0);
//            });
//            input.setStack(0, ItemStack.EMPTY);
//            input.setStack(1, ItemStack.EMPTY);
//        }
//
//        private int getExperience(World world) {
//            int i = this.getExperience(input.getStack(0)) + this.getExperience(input.getStack(1));
//            if (i > 0) {
//                int j = (int) Math.ceil((double) i / 2.0D);
//                return j + world.random.nextInt(j);
//            } else {
//                return 0;
//            }
//        }
//
//        private int getExperience(ItemStack stack) {
//            int i = 0;
//            Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
//            for (Enchantment enchantment : map.keySet()) {
//                if (!enchantment.isCursed()) {
//                    i += enchantment.getMinPower(map.get(enchantment));
//                }
//            }
//
//            return i;
//        }
//
//        public interface BookAndTool {
//            void accept(ItemStack book, ItemStack tool, int bookSlot, int toolSlot);
//        }
//
//        public boolean getBookAndTool(BookAndTool consumer) {
//            int bookSlot = input.getStack(0).isOf(Items.BOOK) ? 0 : input.getStack(1).isOf(Items.BOOK) ? 1 : -1;
//            int toolSlot = input.getStack(0).isDamageable() && input.getStack(0).hasEnchantments() ? 0 : input.getStack(1).isDamageable() && input.getStack(1).hasEnchantments() ? 1 : -1;
//
//            if (bookSlot > -1 && toolSlot > -1) {
//                consumer.accept(input.getStack(bookSlot), input.getStack(toolSlot), bookSlot, toolSlot);
//                return true;
//            }
//
//            return false;
//        }
//
//        public boolean getBookAndTool(BiConsumer<Integer, Integer> consumer) {
//            int bookSlot = input.getStack(0).isOf(Items.BOOK) ? 0 : input.getStack(1).isOf(Items.BOOK) ? 1 : -1;
//            int toolSlot = input.getStack(0).isDamageable() && input.getStack(0).hasEnchantments() ? 0 : input.getStack(1).isDamageable() && input.getStack(1).hasEnchantments() ? 1 : -1;
//
//            if (bookSlot > -1 && toolSlot > -1) {
//                consumer.accept(bookSlot, toolSlot);
//                return true;
//            }
//
//            return false;
//        }
//    }