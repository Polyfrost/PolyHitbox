package org.polyfrost.polyhitbox.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.deftu.omnicore.client.OmniClient;
import dev.deftu.omnicore.client.render.OmniMatrixStack;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.polyfrost.polyhitbox.HitboxRendererKt;
import org.polyfrost.polyhitbox.HitboxState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >=1.21.5
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.EntityHitbox;
//#endif

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinRenderManager {
    //#if MC >=1.21.5
    @Unique
    private static ThreadLocal<Entity> polyhitbox$entity = ThreadLocal.withInitial(() -> null);
    //#endif

    @WrapMethod(
            //#if MC >=1.16.5
            method = "renderHitbox"
            //#else
            //$$ method = "renderDebugBoundingBox"
            //#endif
    )
    //#if MC >=1.16.5
    private static void polyhitbox$customRendering(
         MatrixStack matrixStack,
         VertexConsumer vertexConsumer,
    //#if MC >=1.21.5
         EntityHitbox entityHitbox,
    //#else
    //$$     Entity entity,
    //$$     float tickDelta,
    //#endif
         Operation<Void> operation) {
    //#else
    //$$ private void polyhitbox$customRendering(Entity entity, double offsetX, double offsetY, double offsetZ, float yaw, float tickDelta, CallbackInfo ci) {
    //#endif
        OmniMatrixStack stack = OmniMatrixStack.vanilla(
                //#if MC >=1.16.5
                matrixStack
                //#endif
        );

        //#if MC >=1.21.5
        Entity entity = polyhitbox$entity.get();
        float offsetX = entityHitbox.comp_3855();
        float offsetY = entityHitbox.comp_3856();
        float offsetZ = entityHitbox.comp_3857();
        float collisionBorderSize = 0.0F;
        float width = entity.getWidth();
        //#else
        //$$ float collisionBorderSize = entity.getCollisionBorderSize();
        //$$ float width = entity.width;
        //#endif
        Vec3d lookVec = entity.getRotationVector();

        HitboxRendererKt.renderHitbox(
                stack,
                new HitboxState(
                        offsetX, offsetY, offsetZ,
                        entity.getX(), entity.getY(), entity.getZ(),
                        //#if MC >=1.21.5
                        lookVec.x, lookVec.y, lookVec.z,
                        //#else
                        //$$ lookVec.xCoord, lookVec.yCoord, lookVec.zCoord,
                        //#endif
                        entity.getStandingEyeHeight(),
                        entity instanceof LivingEntity,
                        entity == OmniClient.getInstance().targetedEntity,
                        width,
                        collisionBorderSize,
                        entity.getBoundingBox()
                )
        );
    }

    //#if MC >=1.21.5
    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "renderHitboxes(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityHitboxAndView;Lnet/minecraft/client/render/VertexConsumer;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexRendering;drawVector(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Vector3f;Lnet/minecraft/util/math/Vec3d;I)V"))
    private static boolean polyhitbox$removeViewRay(
        net.minecraft.client.util.math.MatrixStack matrixStack,
        net.minecraft.client.render.VertexConsumer vertexConsumer,
        org.joml.Vector3f vector3f,
        net.minecraft.util.math.Vec3d vec3d,
        int i
    ) {
        return false;
    }

    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private static void polyhitbox$storeEntity(
            Entity entity,
            double x, double y, double z, float tickDelta,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int i,
            CallbackInfo ci
    ) {
        polyhitbox$entity.set(entity);
    }
    //#endif
}
