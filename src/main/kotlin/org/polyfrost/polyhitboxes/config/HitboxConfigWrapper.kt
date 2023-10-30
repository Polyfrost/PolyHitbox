package org.polyfrost.hitboxes.config;

import java.lang.reflect.Field;

public class HitboxConfigWrapper {
    private final Field field;
    public final String category;

    public HitboxConfigWrapper(Field field, String category) {
        this.field = field;
        this.category = category;
    }

    public HitboxConfiguration getOrNull(HitBoxesConfig instance) {
        try {
            return (HitboxConfiguration) field.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    public HitboxConfiguration getCategoryOrDefault(HitBoxesConfig instance) {
        try {
            Global hitboxConfig = (Global) getCategory(instance);
            if (hitboxConfig != null && hitboxConfig.global) return hitboxConfig;
            return (HitboxConfiguration) field.get(instance);
        } catch (Exception e) {
            return instance.player;
        }
    }

    private HitboxConfiguration getCategory(HitBoxesConfig instance) {
        switch (category) {
            case "Passive Entities":
                return instance.passive;
            case "Hostile Entities":
                return instance.hostile;
            case "Projectiles":
                return instance.projectile;
            case "Others":
                return instance.other;
        }
        return null;
    }
}
