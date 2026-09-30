package org.polyfrost.polyhitbox.legacy;

//? if = 1.8.9 {
/*import net.minecraft.client.Options;

public interface OptionsCompat {
    default CameraType getCameraType() {
        CameraType[] types = CameraType.values();
        return types[Math.floorMod(((Options) (Object) this).perspective, types.length)];
    }
}
*///?}
