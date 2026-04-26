package com.NumpaMC.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor("builtChunks")
    ObjectArrayList<ChunkBuilder.BuiltChunk> getBuiltChunks();

    @Accessor("builtChunks")
    void setBuiltChunks(ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks);
}
