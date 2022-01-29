@file:Mixin(ShulkerBoxBlockEntity::class)
@file:JvmName("ShulkerBoxBlockEntityMixinStatic")

package net.orandja.vw.kmixin

import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.maccessors.ShulkerBoxBlockEntityMixinAccessor
import net.orandja.vw.mixinlogic.CloudBoxEntityLogic
import net.orandja.vw.mods.CloudBox
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(ShulkerBoxBlockEntity::class)
abstract class ShulkerBoxBlockEntityMixin(blockEntityType: BlockEntityType<*>?, blockPos: BlockPos?, blockState: BlockState?) : LootableContainerBlockEntity(blockEntityType, blockPos, blockState), SidedInventory,
    ShulkerBoxBlockEntityMixinAccessor {

    var vw_channel: String? = null
    var vw_channelLiteral: String? = null

    override fun setVWChannel(channel: String, literal: String) {
        vw_channel = channel
        vw_channelLiteral = literal
        this.customName = Text.Serializer.fromJson("""{"text":"$vw_channelLiteral"}""")
    }

    override fun isVWChannel(): Boolean {
        return vw_channel != null
    }

    override fun setName(text: Text?) {
        this.customName = text
    }

    @Shadow
    override abstract fun setInvStackList(list: DefaultedList<ItemStack>)

    @Inject(method = ["readNbt"], at = [At("HEAD")], cancellable = true)
    fun fromTag(tag: NbtCompound, info: CallbackInfo) {
        if(tag.contains("vw_channel")) {
            this.invStackList = CloudBox.EMPTY
            super.readNbt(tag)
            vw_channel = tag.getString("vw_channel")
            vw_channelLiteral = tag.getString("vw_channel_literal")
            this.invStackList = CloudBox.getCloudBox(vw_channel!!)
            info.cancel()
        }
    }

    @Inject(method = ["writeNbt"], at = [At("HEAD")], cancellable = true)
    public fun toTag(tag: NbtCompound, info: CallbackInfoReturnable<NbtCompound>) {
        if(vw_channel != null) {
            super.writeNbt(tag)
            Inventories.writeNbt(tag, CloudBox.EMPTY, false)
            tag.putString("vw_channel", vw_channel)
            tag.putString("vw_channel_literal", vw_channelLiteral)
            info.returnValue = tag
        }
    }
}

@Mixin(ShulkerBoxBlock::class)
abstract class ShulkerBoxBlockMixin(settings: Settings?) : BlockWithEntity(settings) {

    @Inject(method = ["onBreak"], at = [At("HEAD")], cancellable = true)
    open fun onBreak(world: World, pos: BlockPos, state: BlockState?, player: PlayerEntity, info: CallbackInfo) {
        val cloudBox = (world.getBlockEntity(pos) as? CloudBoxEntityLogic) ?: return
        if(cloudBox.isVWChannel()) {
            cloudBox.setInv(CloudBox.EMPTY)
            cloudBox.setName(null)
        }
    }

    @Inject(at = [At("RETURN")], method = ["onPlaced"])
    fun channelCloud(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity,
        stack: ItemStack,
        info: CallbackInfo
    ) {
        if(stack.hasNbt()) {
            var channel: String
            var literal: String
            if(stack.nbt!!.contains("vw_channel")) {
                channel = stack.nbt!!.getString("vw_channel")
                literal = stack.nbt!!.getString("vw_channel_literal")
            } else if(stack.nbt!!.contains("BlockEntityTag") && stack.nbt!!.getCompound("BlockEntityTag").contains("vw_channel")) {
                val entityTag = stack.nbt!!.getCompound("BlockEntityTag")
                channel = entityTag.getString("vw_channel")
                literal = entityTag.getString("vw_channel_literal")
            } else {
                return
            }

            CloudBox.getCloudBox(channel) {
                box ->
                val cloudBox = (world.getBlockEntity(pos) as? CloudBoxEntityLogic) ?: return@getCloudBox
                cloudBox.setInv(box)
                cloudBox.setVWChannel(channel, literal)
            }
        }
    }
}