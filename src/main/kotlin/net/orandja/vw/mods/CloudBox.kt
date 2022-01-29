package net.orandja.vw.mods

import net.minecraft.entity.Entity
import net.minecraft.entity.TntEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.orandja.vw.crafting.CloudBoxRecipe
import net.orandja.vw.utils.VWRecipes
import net.orandja.vw.utils.computeLore
import net.orandja.vw.utils.computeTag
import net.orandja.vw.utils.plus
import java.util.function.Consumer
import kotlin.reflect.full.isSuperclassOf

class CloudBox {
    companion object {

        val EMPTY: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
        val CLOUDBOXES: HashMap<String, DefaultedList<ItemStack>> = HashMap()
        val PRIVATECLOUD: HashMap<String, ArrayList<DefaultedList<ItemStack>>> = HashMap()

        fun beforeLaunch() {
            ExtraSaveData.onLoad("cloudboxes") { cloudBoxStorage ->
                cloudBoxStorage.getList("boxes", 10).forEach { compound ->
                    val cloudbox = compound as NbtCompound
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
                CLOUDBOXES.forEach { entry ->
                    val box = NbtCompound()
                    box.putString("channel", entry.key)
                    Inventories.writeNbt(box, entry.value, false)
                    boxes.add(box)
                }

                compound
            }
        }

        fun launch() {
            VWRecipes.addRecipe(CloudBoxRecipe(Identifier("vanillawaifu", "channeledshulkerbox")))
        }

        @JvmStatic
        fun getCloudBox(channel: String, consumer: (DefaultedList<ItemStack>) -> Unit) {
            consumer.invoke(getCloudBox(channel))
        }

        @JvmStatic
        fun getCloudBox(channel: String, consumer: Consumer<DefaultedList<ItemStack>>) {
            consumer.accept(getCloudBox(channel))
        }

        @JvmStatic
        fun getCloudBox(channel: String): DefaultedList<ItemStack> {
            return CLOUDBOXES.getOrPut(channel) { DefaultedList.ofSize(27, ItemStack.EMPTY) }
        }

        @JvmStatic
        fun getCloudBox(channel: CloudChannel): DefaultedList<ItemStack> {
            return CLOUDBOXES.getOrPut(channel.name) { DefaultedList.ofSize(27, ItemStack.EMPTY) }
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
}