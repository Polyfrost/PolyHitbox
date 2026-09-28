package org.polyfrost.polyhitbox.legacy;

//? if = 1.8.9 {
/*import net.minecraft.world.entity.Entity;

public interface EntityCompat {
    default boolean hasIndirectPassenger(Entity passenger) {
        for (Entity rider = ((Entity) (Object) this).rider; rider != null; rider = rider.rider) {
            if (rider == passenger) return true;
        }
        return false;
    }
}
*///?}
