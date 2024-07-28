package org.polyfrost.polyhitbox.mixin;

import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityMobSpawnerRenderer.class)
public class TileEntityMobSpawnerRendererMixin {

    @Inject(method = "renderMob",  at = @At("HEAD"))
    private static void start(MobSpawnerBaseLogic mobSpawnerLogic, double posX, double posY, double posZ, float partialTicks, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.setShouldCancel(true);
    }

    @Inject(method = "renderMob", at = @At("TAIL"))
    private static void end(MobSpawnerBaseLogic mobSpawnerLogic, double posX, double posY, double posZ, float partialTicks, CallbackInfo ci) {
        HitboxRenderer.INSTANCE.setShouldCancel(false);
    }
}