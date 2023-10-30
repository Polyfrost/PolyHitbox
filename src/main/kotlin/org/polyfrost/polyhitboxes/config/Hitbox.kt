package org.polyfrost.polyhitboxes.config

import net.minecraft.entity.Entity
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Hitbox(val value: KClass<out Entity>)
