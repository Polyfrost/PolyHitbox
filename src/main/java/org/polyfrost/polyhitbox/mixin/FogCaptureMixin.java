package org.polyfrost.polyhitbox.mixin;

import org.joml.Vector4f;
import org.polyfrost.polyhitbox.render.HitboxFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

//? if >=1.21.8 {
@Mixin(net.minecraft.client.renderer.fog.FogRenderer.class)
public class FogCaptureMixin {
    @Inject(method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", at = @At("HEAD"))
    private void polyhitbox$captureFog(
        ByteBuffer buffer, int offset, Vector4f color,
        float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd,
        float skyEnd, float cloudEnd,
        CallbackInfo ci
    ) {
        HitboxFog.capture(color.x, color.y, color.z, color.w, environmentalStart, environmentalEnd, renderDistanceStart, renderDistanceEnd);
    }
}
//?} else {
/*@Mixin(net.minecraft.client.renderer.FogRenderer.class)
public class FogCaptureMixin {
}*/
//?}
