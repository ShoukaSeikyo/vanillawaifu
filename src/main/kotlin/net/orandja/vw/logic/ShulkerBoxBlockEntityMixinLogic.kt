package net.orandja.vw.logic

import net.minecraft.entity.Entity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.mods.CloudBox
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

interface ShulkerBoxBlockEntityMixinLogic {

    var channel: CloudBox.CloudChannel?

    fun hasChannel(): Boolean {
        return channel != null
    }

    fun setChannel(name: String, literal: String) {
        this.channel = CloudBox.CloudChannel(name, literal)
    }

    fun setChannel(pair: Pair<String, String>) {
        setChannel(pair.first, pair.second)
    }

    fun setChannel(tag: NbtCompound) {
        this.channel = CloudBox.CloudChannel(tag)
    }

    fun isPublic(): Boolean {
        return this.hasChannel() && channel!!.isPublic()
    }

    fun isOwned(entity: Entity?): Boolean {
        return entity != null && this.hasChannel() && this.channel!!.isOwned(entity)
    }

    fun setBoxInventory(list: DefaultedList<ItemStack>)
    fun setName(text: Text?)

    fun readBoxTag(tag: NbtCompound, info: CallbackInfo, superRead: Consumer<NbtCompound>) {
        if (tag.contains("vw_channel")) {
            this.setBoxInventory(CloudBox.EMPTY)
            superRead.accept(tag)
            setChannel(tag.getString("vw_channel"), tag.getString("vw_channel_literal"))
            this.setBoxInventory(CloudBox.getCloudBox(channel!!))
            info.cancel()
        }
    }

    fun writeBoxTag(tag: NbtCompound, info: CallbackInfo, superWrite: Consumer<NbtCompound>) {
        if (hasChannel()) {
            superWrite.accept(tag)
            Inventories.writeNbt(tag, CloudBox.EMPTY, false)
            this.channel!!.write(tag)
            info.cancel()
        }
    }

    fun clearCloud() {
        if(hasChannel()) {
            setBoxInventory(CloudBox.EMPTY)
            setName(null)
        }
    }
}