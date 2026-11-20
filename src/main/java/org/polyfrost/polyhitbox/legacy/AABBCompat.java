package org.polyfrost.polyhitbox.legacy;

//? if = 1.8.9 {
/*import net.minecraft.world.phys.AABB;

public interface AABBCompat {
    default AABB inflate(double amount) {
        return ((AABB) (Object) this).grown(amount, amount, amount);
    }

    default AABB move(double dx, double dy, double dz) {
        return ((AABB) (Object) this).moved(dx, dy, dz);
    }
}
*///?}
