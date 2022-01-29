//package net.orandja.vw.maccessors
//
//import net.minecraft.enchantment.Enchantment
//import net.minecraft.nbt.NbtCompound
//import net.minecraft.nbt.NbtList
//import net.orandja.vw.mods.EnchantMore
//import java.lang.ref.WeakReference
//
//interface EnchantMoreAccessor {
//
//    fun getEnchantments(): Map<String, Short>
//    fun isEnchanted(): Boolean
//    fun hasEnchantment(enchantment: Enchantment, level: Short = 1): Boolean {
//        return false
//    }
//
//    fun applyEnchantments(name: String, level: Short)
//    fun applyEnchantments(pair: Pair<String, Short>) {
//        applyEnchantments(pair.first, pair.second)
//    }
//
//    fun getProperty(name: String): WeakReference<*> {
//        return WeakReference(null)
//    }
//
//    fun loadEnchants(tag: NbtCompound) {
//        if (tag.contains("Enchantments")) {
//            (tag.get("Enchantments") as NbtList).map(EnchantMore::fromNBT).forEach(this::applyEnchantments)
//        }
//    }
//
//    fun saveEnchants(tag: NbtCompound) {
//        if (this.isEnchanted()) {
//            tag.put("Enchantments", NbtList().apply {
//                getEnchantments().filter(EnchantMore::validEnchant).forEach(EnchantMore.nbtToList(this))
//            })
//        }
//    }
//}