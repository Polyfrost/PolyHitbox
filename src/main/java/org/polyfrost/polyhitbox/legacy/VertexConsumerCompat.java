package org.polyfrost.polyhitbox.legacy;

//? if = 1.8.9 {
/*import com.mojang.blaze3d.vertex.BufferBuilder;

public interface VertexConsumerCompat {
    default BufferBuilder addVertex(float x, float y, float z) {
        return ((BufferBuilder) (Object) this).vertex(x, y, z);
    }

    // Ends the vertex, since modern has no explicit end call
    default BufferBuilder setColor(int argb) {
        BufferBuilder buffer = (BufferBuilder) (Object) this;
        buffer.color(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24);
        buffer.nextVertex();
        return buffer;
    }
}
*///?}
