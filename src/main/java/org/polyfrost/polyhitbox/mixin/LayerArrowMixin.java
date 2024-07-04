package org.polyfrost.polyhitbox.mixin;

import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.entity.EntityLivingBase;
import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArrow.class)
public class LayerArrowMixin {

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    private void start(EntityLivingBase entitylivingbaseIn, float f, float g, float partialTicks, float h, float i, float j, float scale, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.setDrawingLayer(true);
    }

    @Inject(method = "doRenderLayer", at = @At(value = "TAIL"))
    private void end(EntityLivingBase entitylivingbaseIn, float f, float g, float partialTicks, float h, float i, float j, float scale, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.setDrawingLayer(false);
    }

}