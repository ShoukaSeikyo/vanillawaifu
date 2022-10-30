//package net.orandja.vw.logic
//
//import net.minecraft.entity.player.PlayerEntity
//import net.minecraft.item.ItemStack
//import net.minecraft.screen.slot.SlotActionType
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
//
//interface InventoryMiddleClick {
//
//    companion object {
//        val onMiddleClick: MutableList<Pair<(cursorStack: ItemStack) -> Boolean, (cursorStack: ItemStack, player: PlayerEntity) -> Unit>> = mutableListOf()
//    }
//
//    fun checkMiddleClick(cursorStack: ItemStack, slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity, info: CallbackInfo) {
//        if (!player.isCreative && actionType == SlotActionType.CLONE && !cursorStack.isEmpty) {
//            onMiddleClick.firstOrNull{ it.first.invoke(cursorStack) }?.second?.invoke(cursorStack, player)
//        }
//    }
//
//}