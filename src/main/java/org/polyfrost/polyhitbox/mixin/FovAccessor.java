package org.polyfrost.polyhitbox.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// The FOV actually rendered — including the sprint/Speed modifier, the FOV Effects slider and the
// death and lava animations — is private below 26.1, where it moved onto Camera. "useFovSetting"
// is what renderLevel passes for the level projection.
//? if >=26.1 {
@Mixin(net.minecraft.client.Camera.class)
public interface FovAccessor {
    @Invoker("getFov")
    float polyhitbox$fov();
}
//?} elif >=1.21.4 {
/*@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public interface FovAccessor {
    @Invoker("getFov")
    float polyhitbox$fov(net.minecraft.client.Camera camera, float partialTick, boolean useFovSetting);
}*/
//?} else {
/*@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public interface FovAccessor {
    @Invoker("getFov")
    double polyhitbox$fov(net.minecraft.client.Camera camera, float partialTick, boolean useFovSetting);
}*/
//?}
