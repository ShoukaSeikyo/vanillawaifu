package net.orandja.vanillawaifu.mixin;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Tickable;
import net.orandja.vanillawaifu.VanillaWaifu;
import net.orandja.vanillawaifu.utils.BlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@EnvironmentInterfaces({@EnvironmentInterface(
        value = EnvType.CLIENT,
        itf = ChestAnimationProgress.class
)})
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin extends LootableContainerBlockEntity implements ChestAnimationProgress, Tickable {

    List<String> whitelist = Lists.newArrayList();

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/block/entity/BlockEntityType;)V")
    private void init(BlockEntityType<?> blockEntityType, CallbackInfo info) {
        BlockUtils.<String>observe(this, "whitelist", whitelist::add);
        BlockUtils.accessor(this, "whitelist", () -> whitelist);
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    public void fromTag(BlockState state, NbtCompound tag, CallbackInfo info) {
        if(!tag.contains("whitelist"))
            return;

        tag.getList("whitelist", 8).stream().map(NbtElement::asString).forEach(whitelist::add);
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    public void toTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> info) {
        if(whitelist.size() == 0)
            return;

        NbtList listTag = new NbtList();
        whitelist.stream().map(NbtString::of).forEach(listTag::add);
        tag.put("whitelist", listTag);

//        if (this.getCustomName() != null) {
//            tag.putString("CustomName", Text.Serializer.toJson(new LiteralText(getCustomName().asString() + "\r\n" + "Efficiency V" + "\r\n" + "Efficiency V" + "\r\n" + "Efficiency V" + "\r\n" + "Efficiency V" + "\r\n" + "Efficiency V")));
//        }
    }
}
