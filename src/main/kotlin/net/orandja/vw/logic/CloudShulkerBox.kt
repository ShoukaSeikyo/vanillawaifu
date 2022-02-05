package net.orandja.vw.logic

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.TntEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.orandja.mcutils.*
import net.orandja.vw.ExtraSaveData
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer
import kotlin.reflect.full.isSuperclassOf

interface CloudShulkerBox {


    companion object {

        val EMPTY: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
        var CLOUDBOXES: HashMap<String, DefaultedList<ItemStack>> = HashMap()

        fun beforeLaunch() {
            ExtraSaveData.onLoad("cloudboxes") {
                CLOUDBOXES = HashMap()
                it.getList("boxes", 10).forEach {
                    val cloudbox = it as NbtCompound
                    val channel = cloudbox.getString("channel")
                    getCloudBox(channel) { inventory ->
                        if (cloudbox.contains("Items", 9)) {
                            Inventories.readNbt(cloudbox, inventory)
                        }
                    }
                }
            }

            ExtraSaveData.onSave("cloudboxes") {
                val compound = NbtCompound()
                val boxes = NbtList()
                compound.put("boxes", boxes)
                CLOUDBOXES.forEach {
                    val box = NbtCompound()
                    box.putString("channel", it.key)
                    Inventories.writeNbt(box, it.value, false)
                    boxes.add(box)
                }

                compound
            }
            CustomRecipe.customShapelessRecipes[Identifier("vanillawaifu", "cloudbox")] = ::CloudBoxRecipe
        }

        fun getCloudBox(channel: String, consumer: (DefaultedList<ItemStack>) -> Unit) {
            consumer.invoke(getCloudBox(channel))
        }

        fun getCloudBox(channel: String, consumer: Consumer<DefaultedList<ItemStack>>) {
            consumer.accept(getCloudBox(channel))
        }

        fun getCloudBox(channel: String): DefaultedList<ItemStack> {
            return CLOUDBOXES.getOrPut(channel) { DefaultedList.ofSize(27, ItemStack.EMPTY) }
        }

        fun getCloudBox(channel: CloudChannel): DefaultedList<ItemStack> {
            return CLOUDBOXES.getOrPut(channel.name) { DefaultedList.ofSize(27, ItemStack.EMPTY) }
        }
    }

    var channel: CloudChannel?

    fun hasChannel(): Boolean {
        return channel != null
    }

    fun setChannel(name: String, literal: String) {
        this.channel = CloudChannel(name, literal)
    }

    fun setChannel(pair: Pair<String, String>) {
        setChannel(pair.first, pair.second)
    }

    fun setChannel(tag: NbtCompound) {
        this.channel = CloudChannel(tag)
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
            this.setBoxInventory(EMPTY)
            superRead.accept(tag)
            setChannel(tag.getString("vw_channel"), tag.getString("vw_channel_literal"))
            this.setBoxInventory(getCloudBox(channel!!))
            info.cancel()
        }
    }

    fun writeBoxTag(tag: NbtCompound, info: CallbackInfo, superWrite: Consumer<NbtCompound>) {
        if (hasChannel()) {
            superWrite.accept(tag)
            Inventories.writeNbt(tag, EMPTY, false)
            this.channel!!.write(tag)
            info.cancel()
        }
    }

    fun clearCloud() {
        if(hasChannel()) {
            setBoxInventory(EMPTY)
            setName(null)
        }
    }

    fun clearCloud(world: World, pos: BlockPos) {
        (world.getBlockEntity(pos) as? CloudShulkerBox)?.clearCloud()
    }

    fun channelCloud(world: World, pos: BlockPos, stack: ItemStack) {
        if (stack.hasNbt()) {
            val channelPair = CloudChannel.getCloudChannel(stack) ?: return

            getCloudBox(channelPair.first) { box: DefaultedList<ItemStack> ->
                val cloudBox = (world.getBlockEntity(pos) as? CloudShulkerBox) ?: return@getCloudBox
                cloudBox.setBoxInventory(box)
                cloudBox.setChannel(channelPair)
            }
        }
    }

    fun checkCloud(getStacks: (BlockState, LootContext.Builder) -> List<ItemStack>, state: BlockState, builder: LootContext.Builder, info: CallbackInfoReturnable<List<ItemStack>>) {
        val cloudBox = (builder.getNullable(LootContextParameters.BLOCK_ENTITY) as? CloudShulkerBox) ?: return
        if(!cloudBox.hasChannel()) {
            return
        }

        if(!cloudBox.isPublic() && !cloudBox.isOwned(builder.getNullable(LootContextParameters.THIS_ENTITY))) {
            return
        }

        cloudBox.setBoxInventory(EMPTY)
        cloudBox.setName(null)
        val loots = getStacks.invoke(state, builder)

        loots.forEach(cloudBox.channel!!::writeStack)
        info.returnValue = loots

    }

}



class CloudChannel(val name: String, val literal: String) {

    companion object {
        fun getCloudChannel(tag: NbtCompound): Pair<String, String>? {
            if(tag.contains("vw_channel")) {
                return Pair(tag.getString("vw_channel"), tag.getString("vw_channel_literal"))
            }

            if(tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("vw_channel")) {
                val entityTag = tag.getCompound("BlockEntityTag")
                return Pair(entityTag.getString("vw_channel"), entityTag.getString("vw_channel_literal"))
            }

            return null
        }

        fun getCloudChannel(stack: ItemStack): Pair<String, String>? {
            if(stack.hasNbt()) {
                return getCloudChannel(stack.nbt!!)
            }

            return null
        }
    }

    constructor(tag: NbtCompound): this(tag.getString("vw_channel"), tag.getString("vw_channel_literal"))

    fun isPublic(): Boolean {
        return name.startsWith("public:")
    }

    fun isOwned(entity: Entity?): Boolean {
        if (!isPublic() && entity != null) {
            if (PlayerEntity::class.isSuperclassOf(entity::class)) {
                return name.startsWith("${entity.uuidAsString}:")
            }

            if (TntEntity::class.isSuperclassOf(entity::class)) {
                return isOwned((entity as TntEntity).causingEntity)
            }
        }

        return false
    }

    fun asText(): Text {
        return Text.Serializer.fromJson("""{"text":"$literal"}""") as Text
    }

    fun write(tag: NbtCompound) {
        tag.putString("vw_channel", name)
        tag.putString("vw_channel_literal", literal)
    }

    fun writeStack(stack: ItemStack) {
        stack.computeTag(::write)

        stack.computeLore { lore ->
            lore.clear()
            lore + """{"text":"$literal", "color":"blue"}"""
        }

        stack.setCustomName(Text.Serializer.fromJson("""{"text":"[Cloud Box] ","color":"green"}"""))
    }
}

class CloudBoxRecipe(identifier: Identifier, group: String, output: ItemStack, input: DefaultedList<Ingredient>) : ShapelessRecipe(identifier, group, output, input) {

    override fun isIgnoredInRecipeBook(): Boolean {
        return true
    }

    override fun getOutput(): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun matches(craftingInventory: CraftingInventory, world: World?): Boolean {
        return super.matches(craftingInventory, world) && craftingInventory.toArray().find {
            Block.getBlockFromItem(it.item) is ShulkerBoxBlock && it.hasCustomName() && it.name.asString().length > 2 &&
                    !it.nbt!!.contains("vw_channel") &&
                    (!it.nbt!!.contains("BlockEntityTag") || !it.nbt!!.getCompound("BlockEntityTag").contains("Items") || it.nbt!!.getCompound("BlockEntityTag").getList("Items", 10).size == 0)
        } != null
    }

    override fun craft(craftingInventory: CraftingInventory): ItemStack? {
        val output = craftingInventory.toArray().firstOrNull { stack -> Block.getBlockFromItem(stack.item) is ShulkerBoxBlock && stack.hasCustomName() }?.copy() ?: return ItemStack.EMPTY

        var channel = output.name.asString()
        var channelLiteral: String
        if (channel.startsWith(":", true)) {
            val player = craftingInventory.getPlayer() ?: return output
            channel = channel.substring(1);
            channelLiteral = "${channel} of ${player.entityName}"
            channel = "${player.uuidAsString}:${channel}"
        } else {
            channelLiteral = "$channel of public"
            channel = "public:${channel}"
        }

        output.computeTag { tag ->
            tag.putString("vw_channel", channel)
            tag.putString("vw_channel_literal", channelLiteral)
            tag.getOrCompute("Enchantments", { tag.get("Enchantments") as NbtList }, ::NbtList) {}
        }

        output.computeLore { lore ->
            lore.clear()
            lore + """{"text":"$channelLiteral", "color":"blue"}"""
        }

        output.setCustomName(Text.Serializer.fromJson("""{"text":"[Cloud Box] ","color":"green"}"""))

        return output
    }
}