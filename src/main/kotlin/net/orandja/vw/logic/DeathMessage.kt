//package net.orandja.vw.logic
//
//import net.minecraft.entity.LivingEntity
//import net.minecraft.text.*
//
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
//
//
//interface DeathMessage {
//
//    val entity: LivingEntity
//
//    fun sendDeathPosition(info: CallbackInfoReturnable<Text>) {
//        val text = info.returnValue as? MutableText ?: return
//        val pos = entity.blockPos;
//        Text.of(" [${ pos.x }; ${ pos.y }; ${ pos.z }] in " + entity.entityWorld.registryKey.value.toString())
//            .getWithStyle(Style.EMPTY.withColor(TextColor.parse("green")))
//            .forEach(text::append)
//    }
//
//}