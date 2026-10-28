package org.polyfrost.polyhitbox.mixin;

//? if >1.8.9
import net.minecraft.client.Camera;
//? if = 1.8.9 {
/*import net.minecraft.client.render.GameRenderer;
*///?} elif <26.1 {
/*import net.minecraft.client.renderer.GameRenderer;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// The rendered FOV is private below 26.1 where it moved onto Camera
// useFovSetting is what renderLevel passes for the level projection
//? if >=26.1 {
@Mixin(Camera.class)
public interface FovAccessor {
    @Invoker("getFov")
    float polyhitbox$fov();
}
//?} elif >=1.21.4 {
/*@Mixin(GameRenderer.class)
public interface FovAccessor {
    @Invoker("getFov")
    float polyhitbox$fov(Camera camera, float partialTick, boolean useFovSetting);
}
*///?} elif >1.8.9 {
/*@Mixin(GameRenderer.class)
public interface FovAccessor {
    @Invoker("getFov")
    double polyhitbox$fov(Camera camera, float partialTick, boolean useFovSetting);
}*/
//?} else {
/*@Mixin(GameRenderer.class)
public interface FovAccessor {
    @Invoker("getFov")
    float polyhitbox$fov(float partialTick, boolean useFovSetting);
}*/
//?}
