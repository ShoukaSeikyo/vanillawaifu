package net.orandja.vw.logic

import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.crafting.DeepStorageScreenHandler
import net.orandja.vw.logic2.BlockWithEnchantment
import net.orandja.vw.utils.ofSize
import net.orandja.vw.utils.readNbt
import net.orandja.vw.utils.writeNbt
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

interface DeepBarrelLogic: BlockWithEnchantment {

    var infinity: Short
    var efficiency: Short
    var inventory: DefaultedList<ItemStack>


    override fun hasEnchantments(): Boolean {
        return infinity > 0 || efficiency > 0
    }

    override fun getEnchantments(): Map<String, Short> {
        return java.util.Map.of(
            "efficiency", efficiency,
            "infinity", infinity
        )
    }

    override fun applyEnchantments(name: String, level: Short) {
        when (name) {
            "infinity" -> infinity = level
            "efficiency" -> efficiency = level
        }
        if (this.infinity > 0) {
            this.inventory = ofSize(27 + 27 * (infinity + efficiency), 27, ItemStack.EMPTY)
        }
    }

    fun createBarrelScreenHandler(barrel: Object, syncId: Int, playerInventory: PlayerInventory, info: CallbackInfoReturnable<ScreenHandler>) {
        if (this.infinity > 0) {
            info.returnValue = DeepStorageScreenHandler(syncId, playerInventory, barrel as BarrelBlockEntity)
        }
    }

    fun writeNbt(tag: NbtCompound, info: CallbackInfo, superWrite: Consumer<NbtCompound>) {
        if (infinity > 0) {
            saveEnchantments(tag)
            writeNbt(tag, this.inventory)
            superWrite.accept(tag)
            info.cancel()
        }
    }

    fun readNbt(tag: NbtCompound, info: CallbackInfo, superRead: Consumer<NbtCompound>) {
        loadEnchantments(tag)
        if (infinity > 0) {
            this.inventory = ofSize(27 + 27 * (infinity + efficiency), 27, ItemStack.EMPTY)
            readNbt(tag, this.inventory)
            superRead.accept(tag)
            info.cancel()
        }
    }

}