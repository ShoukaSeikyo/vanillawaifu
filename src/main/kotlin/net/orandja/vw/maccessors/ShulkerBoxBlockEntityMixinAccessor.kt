package net.orandja.vw.maccessors

import net.minecraft.entity.Entity
import net.minecraft.text.Text

interface ShulkerBoxBlockEntityMixinAccessor {
    fun setVWChannel(channel: String, literal: String)
    fun isVWChannel(): Boolean
    fun isVWPublic(): Boolean
    fun isVWOwned(entity: Entity?): Boolean
    fun setName(text: Text?)
}