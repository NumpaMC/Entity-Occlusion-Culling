package com.NumpaMC.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class BlockEntityRenderManagerMixin {

    private static final int SAFE_RADIUS_SECTIONS = 1;   // Chỉ giữ chunk cực gần người chơi
    private static final int GUARD_BAND_SECTIONS = 0;    // Không đệm gì cả

    // Throttle: cull mỗi ~150ms để cân bằng giữa hiệu năng và phản hồi nhanh
    private static long lastCullTime = 0;
    private static final long CULL_INTERVAL_MS = 150;

    @Inject(method = "render", at = @At("TAIL"))
    private void bura$aggressiveChunkCulling(CallbackInfo ci) {
        long now = System.currentTimeMillis();
        if (now - lastCullTime < CULL_INTERVAL_MS) {
            return;
        }
        lastCullTime = now;

        WorldRenderer wr = (WorldRenderer) (Object) this;
        Frustum frustum = wr.getCapturedFrustum();
        if (frustum == null) return;

        WorldRendererAccessor accessor = (WorldRendererAccessor) (Object) this;
        ObjectArrayList<ChunkBuilder.BuiltChunk> visible = accessor.getBuiltChunks();
        if (visible == null || visible.isEmpty()) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getCameraPos();           // giữ theo mapping của bạn
        BlockPos camBlock = BlockPos.ofFloored(camPos);

        ObjectArrayList<ChunkBuilder.BuiltChunk> filtered = new ObjectArrayList<>(visible.size());

        for (ChunkBuilder.BuiltChunk chunk : visible) {
            BlockPos origin = chunk.getOrigin();

            int dx = (origin.getX() - camBlock.getX()) >> 4;
            int dy = (origin.getY() - camBlock.getY()) >> 4;
            int dz = (origin.getZ() - camBlock.getZ()) >> 4;

            // 1. Chunk cực gần → luôn giữ (tránh pop-in ngay sát người chơi)
            if (Math.abs(dx) <= SAFE_RADIUS_SECTIONS &&
                    Math.abs(dy) <= SAFE_RADIUS_SECTIONS &&
                    Math.abs(dz) <= SAFE_RADIUS_SECTIONS) {
                filtered.add(chunk);
                continue;
            }

            // 2. Chunk xa → chỉ giữ nếu Frustum nói VISIBLE (sau lưng, bị block chắn, ngoài góc nhìn → cull hết)
            boolean visibleInFrustum = frustum.isVisible(chunk.getBoundingBox());

            if (visibleInFrustum) {
                filtered.add(chunk);
            }
        }

        accessor.setBuiltChunks(filtered);
    }
}
