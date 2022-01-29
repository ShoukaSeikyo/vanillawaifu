package net.orandja.vw.utils

import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.reflect.KClass
import kotlin.reflect.cast

//fun <B> getBlockEntityAccessor(world: World, pos: BlockPos, valid: (B) -> Any) {
//    val entity = world.getBlockEntity(pos) as? Object as? B ?: return
//    valid.invoke(entity)
//}

//fun getBlockEntity(world: World, pos: BlockPos, clazz: KClass<*>, valid: (Any) -> Unit) {
//    val entity = world.getBlockEntity(pos)
//    if (clazz.isInstance(entity)) {
//        valid.invoke(clazz.cast(entity))
//    }
//}
//
//fun getBlockEntity(world: World, pos: BlockPos, clazz: KClass<*>): Any? {
//    val entity = world.getBlockEntity(pos)
//    return if (clazz.isInstance(entity)) {
//        clazz.cast(entity)
//    } else
//        null
//}
//
//fun getBlockEntityMixin(entity: BlockEntity?, clazz: KClass<*>): Any? {
//    return if (clazz.isInstance(entity)) {
//        clazz.cast(entity)
//    } else
//        null
//}
//
//fun <B, M> getBlockEntityMixin(world: World, pos: BlockPos, valid: (M) -> Unit) {
//    val entity = world.getBlockEntity(pos) as? B as? M ?: return
//    valid.invoke(entity)
//}