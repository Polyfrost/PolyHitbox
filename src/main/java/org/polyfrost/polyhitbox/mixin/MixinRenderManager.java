package org.polyfrost.polyhitbox.mixin;

import dev.deftu.omnicore.api.client.OmniClient;
import dev.deftu.omnicore.api.client.render.stack.OmniMatrixStack;
import dev.deftu.omnicore.api.client.render.stack.OmniMatrixStacks;
import dev.deftu.omnicore.api.data.aabb.OmniAABB;
import dev.deftu.omnicore.api.data.vec.OmniVec3d;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.polyfrost.polyhitbox.HitboxRendererKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >=1.21.5
import net.minecraft.client.render.entity.state.EntityHitbox;
//#endif

//#if MC >=1.16.5
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Unique;
//#endif

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinRenderManager {
    //#if MC >=1.21.4
    @Unique
    private static ThreadLocal<Entity> polyhitbox$entity = ThreadLocal.withInitial(() -> null);
    //#endif

    @Inject(
            //#if MC >=1.16.5
            method = "renderHitbox",
            //#else
            //$$ method = "renderDebugBoundingBox",
            //#endif
            at = @At("HEAD"),
            cancellable = true
    )
    //#if MC >=1.16.5
    private static void polyhitbox$customRendering(
            MatrixStack matrixStack,
            VertexConsumer vertexConsumer,
            //#if MC >=1.21.5
            EntityHitbox entityHitbox,
            //#else
            //$$ Entity entity,
            //$$ float tickDelta,
            //#endif
            //#if MC >=1.21.1
            //$$ float offsetX, float offsetY, float offsetZ,
            //#endif
            CallbackInfo ci
    ) {
    //#else
    //$$ private void polyhitbox$customRendering(Entity entity, double offsetX, double offsetY, double offsetZ, float yaw, float tickDelta, CallbackInfo ci) {
    //#endif
        ci.cancel();
        OmniMatrixStack stack = OmniMatrixStacks.vanilla(
                //#if MC >=1.16.5
                matrixStack
                //#endif
        );

        //#if MC >=1.21.5
        Entity entity = polyhitbox$entity.get();
        OmniVec3d offset = new OmniVec3d(entityHitbox.comp_3855(), entityHitbox.comp_3856(), entityHitbox.comp_3857());
        //#else
        //$$ OmniVec3d offset = OmniVec3d.ZERO;
        //#endif

        OmniVec3d entityPosition = new OmniVec3d(
                //#if MC >=1.16.5
                entity.getX(), entity.getY(), entity.getZ()
                //#else
                //$$ entity.x, entity.y, entity.z
                //#endif
        );
        OmniVec3d lookVec = new OmniVec3d(entity.getRotationVector());
        OmniAABB entityAABB = new OmniAABB(entity.getBoundingBox());

        HitboxRendererKt.renderHitbox(
                stack,
                offset,
                entityPosition,
                lookVec,
                entity.getStandingEyeHeight(),
                entity instanceof LivingEntity,
                entity == OmniClient.get().targetedEntity,
                //#if MC >=1.16.5
                entity.getWidth(),
                0.0F, // TODO/NOTE: Doesn't exist?
                //#else
                //$$ entity.width,
                //$$ entity.getCollisionBorderSize(),
                //#endif
                entityAABB
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
