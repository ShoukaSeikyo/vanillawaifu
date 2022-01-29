package net.orandja.vanillawaifu.crafting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;
import java.util.List;

public class AutomatedCraftingInventory extends CraftingInventory {
    private final DefaultedList<ItemStack> stacks;
    private final int width;
    private final int height;

    public AutomatedCraftingInventory(int width, int height, List<ItemStack> stacks) {
        super(null, width, height);
        this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
        for(int i = 0; i < stacks.size(); i++) {
            this.stacks.set(i, stacks.get(i));
        }
        this.width = width;
        this.height = height;
    }

    public int size() {
        return this.stacks.size();
    }

    public boolean isEmpty() {
        Iterator var1 = this.stacks.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public ItemStack getStack(int slot) {
        return slot >= this.size() ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
    }

    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.stacks, slot, amount);
    }

    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
    }

    public void markDirty() {
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
        this.stacks.clear();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void provideRecipeInputs(RecipeMatcher finder) {
        Iterator var2 = this.stacks.iterator();

        while(var2.hasNext()) {
            ItemStack itemStack = (ItemStack)var2.next();
            finder.addInput(itemStack);
        }

    }
}
