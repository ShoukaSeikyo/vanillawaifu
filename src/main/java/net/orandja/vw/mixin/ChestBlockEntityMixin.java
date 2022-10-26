//package net.orandja.vw.mixin;
//
//import com.google.common.collect.Lists;
//import lombok.Getter;
//import lombok.Setter;
//import net.minecraft.block.entity.ChestBlockEntity;
//import net.minecraft.nbt.NbtCompound;
//import net.orandja.vw.logic.WhitelistedChestBlock;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.ArrayList;
//
//@SuppressWarnings("unused")
//@Mixin(ChestBlockEntity.class)
//public abstract class ChestBlockEntityMixin implements WhitelistedChestBlock {
//
//    @Getter @Setter ArrayList<String> whitelist = Lists.newArrayList();
//
//    @Inject(method = "readNbt", at = @At("RETURN"))
//    void readNbt(NbtCompound nbt, CallbackInfo info) {
//        loadWhitelist(nbt);
//    }
//
//    @Inject(method = "writeNbt", at = @At("RETURN"))
//    void writeNbt(NbtCompound nbt, CallbackInfo info) {
//        saveWhitelist(nbt);
//    }
//}
