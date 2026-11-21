package org.polyfrost.polyhitbox.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
//? if >1.8.9 {
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
//?} else {
/*import net.minecraft.world.level.Level;
*///?}
import org.polyfrost.polyhitbox.render.HitboxRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if = 1.8.9
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >1.8.9 {
@Mixin(ClientLevel.class)
public class BlockCacheMixin {
    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void polyhitbox$blockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        HitboxRenderer.onBlocksChanged();
    }

    @Inject(method = "onChunkLoaded", at = @At("HEAD"))
    private void polyhitbox$chunkLoaded(ChunkPos pos, CallbackInfo ci) {
        HitboxRenderer.onBlocksChanged();
    }

    @Inject(method = "unload", at = @At("HEAD"))
    private void polyhitbox$chunkUnloaded(LevelChunk chunk, CallbackInfo ci) {
        HitboxRenderer.onBlocksChanged();
    }
}
//?} else {
/*@Mixin(Level.class)
public class BlockCacheMixin {
    @Inject(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z", at = @At("HEAD"))
    private void polyhitbox$blockChanged(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientLevel) HitboxRenderer.onBlocksChanged();
    }

    @Inject(method = "notifyRegionChanged(IIIIII)V", at = @At("HEAD"))
    private void polyhitbox$regionChanged(CallbackInfo ci) {
        if ((Object) this instanceof ClientLevel) HitboxRenderer.onBlocksChanged();
    }
}
*///?}
