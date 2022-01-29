package net.orandja.vw.accessor;

import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DefaultedList.class)
public interface DefaultedListAccessor {
    @Accessor
    List<?> getDelegate();
}
