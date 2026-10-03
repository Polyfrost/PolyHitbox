package org.polyfrost.polyhitbox.legacy;

//? if = 1.8.9 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public interface LevelRendererCompat {
    // 1.8.9 only skips entities in unloaded chunks, and Argentum replaces the chunk storage
    default boolean isSectionCompiled(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null && level.isChunkLoaded(pos);
    }
}
*///?}
