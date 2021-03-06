package net.orandja.mcutils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString

fun List<NbtElement>.toNBTList(): NbtList =
    NbtList().apply list@{ this@toNBTList.forEach { this@list.add(it) } }

operator fun NbtList.plus(string: String): NbtList = run {
    this.add(NbtString.of(string))
    return this
}
operator fun NbtList.plus(element: NbtElement): NbtList = run {
    this.add(element)
    return this
}

fun <N: NbtElement> NbtCompound.getOrCompute(id: String, getter: (String) -> N?, compute: () -> N?, consumer: (N) -> Unit): N {
    var value: N?
    if(this.contains(id))
        value = getter(id)
    else {
        value = compute()
        this.put(id, value)
    }

    consumer.invoke(value!!)
    return value
}

fun <N: NbtElement> NbtCompound.getOrCreate(id: String, getter: (String) -> N?, compute: () -> N?): N {
    return if(this.contains(id))
        getter(id)!!
    else
        compute().apply { this@getOrCreate.put(id, this) }!!
}

fun NbtCompound.getTagOrCompute(id: String, consumer: (NbtCompound) -> Unit): NbtCompound {
    return getOrCompute(id, this::getCompound, ::NbtCompound, consumer)
}

fun NbtCompound.getSLOrCompute(id: String, compute: () -> NbtList = ::NbtList, consumer: (NbtList) -> Unit): NbtList {
    return getOrCompute(id, this::getStringList, compute, consumer)
}

fun NbtCompound.getStringList(id: String): NbtList {
    return this.getList(id, 8)
}

fun ItemStack.computeLore(consumer: (NbtList) -> Unit) {
    this.computeTag {
        it.getTagOrCompute("display") { display ->
            display.getSLOrCompute("Lore", ::NbtList, consumer)
        }
    }
}

fun NbtList._addAll(collection: Iterable<NbtString>) = apply { collection.forEach(this::add) }