package net.orandja.vw.logic

import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.vw.mods.CloudBox
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

interface ShulkerBoxBlockMixinLogic {

    fun clearCloud(world: World, pos: BlockPos) {
        (world.getBlockEntity(pos) as? ShulkerBoxBlockEntityMixinLogic)?.clearCloud()
    }

    fun channelCloud(world: World, pos: BlockPos, stack: ItemStack) {
        if (stack.hasNbt()) {
            val channelPair = CloudBox.CloudChannel.getCloudChannel(stack) ?: return

            CloudBox.getCloudBox(channelPair.first) { box: DefaultedList<ItemStack> ->
                val cloudBox = (world.getBlockEntity(pos) as? ShulkerBoxBlockEntityMixinLogic) ?: return@getCloudBox
                cloudBox.setBoxInventory(box)
                cloudBox.setChannel(channelPair)
            }
        }
    }

    fun checkCloud(getStacks: (BlockState, LootContext.Builder) -> List<ItemStack>, state: BlockState, builder: LootContext.Builder, info: CallbackInfoReturnable<List<ItemStack>>) {
        val cloudBox = (builder.getNullable(LootContextParameters.BLOCK_ENTITY) as? ShulkerBoxBlockEntityMixinLogic) ?: return
        if(!cloudBox.hasChannel()) {
            return
        }

        if(!cloudBox.isPublic() && !cloudBox.isOwned(builder.getNullable(LootContextParameters.THIS_ENTITY))) {
            return
        }

        cloudBox.setBoxInventory(CloudBox.EMPTY)
        cloudBox.setName(null)
        val loots = getStacks.invoke(state, builder)

        loots.forEach(cloudBox.channel!!::writeStack)
        info.returnValue = loots

    }
}