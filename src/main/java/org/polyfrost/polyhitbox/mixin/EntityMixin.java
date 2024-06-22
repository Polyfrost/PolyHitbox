package org.polyfrost.polyhitbox.mixin;

import net.minecraft.entity.Entity;
import org.polyfrost.polyhitbox.config.HitboxCategory;
import org.polyfrost.polyhitbox.config.HitboxConfig;
import org.polyfrost.polyhitbox.config.ModConfig;
import org.polyfrost.polyhitbox.hooks.EntityHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements EntityHook {
    @Unique private HitboxConfig polyHitbox$hitboxConfig;
    @Unique private boolean polyHitbox$hitboxConfigChecked;

    @Override
    public HitboxConfig polyHitbox$getHitboxConfig() {
        if (!polyHitbox$hitboxConfigChecked) {
            for (HitboxCategory config : ModConfig.INSTANCE.getSortedByPriority()) {
                if (config.getCondition().invoke((Entity) (Object) this)) {
                    polyHitbox$hitboxConfig = config.getConfig();
                    break;
                }
            }
            polyHitbox$hitboxConfigChecked = true;
        }
        if (polyHitbox$hitboxConfig != null && polyHitbox$hitboxConfig.getOverwriteDefault()) {
            return polyHitbox$hitboxConfig;
        } else {
            return HitboxCategory.DEFAULT.getConfig();
        }
    }
}
