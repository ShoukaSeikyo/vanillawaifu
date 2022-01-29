package net.orandja.vw.accessor;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DispenserBlockEntity.class)
public interface DispenserBlockEntityAccessor {
    @Accessor
    DefaultedList<ItemStack> getInventory();
}
