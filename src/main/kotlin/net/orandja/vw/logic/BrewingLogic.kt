//package net.orandja.vw.logic
//
//import net.minecraft.block.entity.BrewingStandBlockEntity
//import net.minecraft.enchantment.Enchantment
//import net.minecraft.enchantment.Enchantments
//import net.minecraft.util.math.MathHelper
//import net.orandja.vw.logic2.BlockWithEnchantment
//import java.lang.ref.WeakReference
//
//interface BrewingLogic: BlockWithEnchantment {
//
//    var brewTime: Int
//    var fuel: Int
//    var baneOfArthropods: Short
//    var efficiency: Short
//    var unbreaking: Short
//    var fireAspect: Short
//    var silkTouch: Short
//
//    override fun getEnchantments(): Map<String, Short> {
//        return mapOf(
//            "efficiency" to efficiency,
//            "bane_of_arthropods" to baneOfArthropods,
//            "silk_touch" to silkTouch,
//            "unbreaking" to unbreaking,
//            "fire_aspect" to fireAspect
//        )
//    }
//
//    override fun hasEnchantments(): Boolean {
//        return baneOfArthropods > 0 || efficiency > 0 || unbreaking > 0 || fireAspect > 0 || silkTouch > 0
//    }
//
//    override fun applyEnchantments(name: String, level: Short) {
//        when (name) {
//            "bane_of_arthropods" -> baneOfArthropods = level
//            "efficiency" -> efficiency = level
//            "unbreaking" -> unbreaking = level
//            "silk_touch" -> silkTouch = level
//            "fire_aspect" -> fireAspect = level
//        }
//    }
//
//    override fun hasEnchantment(enchantment: Enchantment, level: Short): Boolean {
//        when(enchantment) {
//            Enchantments.SILK_TOUCH -> return silkTouch >= level
//            Enchantments.EFFICIENCY -> return efficiency >= level
//            Enchantments.UNBREAKING -> return unbreaking >= level
//            Enchantments.FIRE_ASPECT -> return fireAspect >= level
//            Enchantments.BANE_OF_ARTHROPODS -> return baneOfArthropods >= level
//        }
//
//        return false
//    }
//
//    companion object {
//        fun accelerate(stand: BrewingStandBlockEntity) {
//            if(stand is BrewingLogic && (stand.efficiency + stand.baneOfArthropods > 0))
//                stand.brewTime = MathHelper.clamp(stand.brewTime + 1 - Math.max(1, (stand.efficiency + stand.baneOfArthropods) * 2), 0, 400)
//        }
//
//        fun setFuelCount(stand: BrewingStandBlockEntity?, fuel: Int) {
//            if(stand is BrewingLogic)
//                stand.fuel = fuel + fuel * (stand.unbreaking + stand.fireAspect) / 5
//        }
//    }
//}