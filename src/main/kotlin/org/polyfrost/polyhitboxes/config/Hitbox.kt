package org.polyfrost.hitboxes.config;

import net.minecraft.entity.Entity;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Hitbox {
    Class<? extends Entity> value();
}
