/*
package yt.Numpamc.frustumcullingentity.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import yt.Numpamc.frustumcullingentity.CacheEntry;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(ProjectileEntityRenderer.class)
public abstract class ProjectileEntityRendererMixin {

    @Unique
    private static final double MAX_RENDER_DISTANCE = 110.0;
    @Unique
    private static final long CACHE_DURATION_MS = 250L;

    // dùng cache theo object state, ko cần entity
    @Unique
    private final Map<ProjectileEntityRenderState, CacheEntry> visibilityCache = new WeakHashMap<>();

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ProjectileEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRender(
            ProjectileEntityRenderState state,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.gameRenderer == null) return;

        long now = System.currentTimeMillis();
        CacheEntry cached = visibilityCache.get(state);
        if (cached != null && (now - cached.lastCheck < CACHE_DURATION_MS)) {
            if (!cached.visible) ci.cancel();
            return;
        }

        // Lấy toạ độ từ render state
        Vec3d projectilePos = new Vec3d(state.x, state.y, state.z);
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        double distSq = cameraPos.squaredDistanceTo(projectilePos);
        if (distSq > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            visibilityCache.put(state, new CacheEntry(false, now));
            ci.cancel();
            return;
        }

        // tạo box ảo xung quanh projectile
        Box box = new Box(
                projectilePos.x - 0.25, projectilePos.y - 0.25, projectilePos.z - 0.25,
                projectilePos.x + 0.25, projectilePos.y + 0.25, projectilePos.z + 0.25
        );

        boolean visible = computeVisibility(client, cameraPos, box, projectilePos);
        visibilityCache.put(state, new CacheEntry(visible, now));
        if (!visible) ci.cancel();
    }

    @Unique
    private boolean computeVisibility(MinecraftClient client, Vec3d cameraPos, Box entityBox, Vec3d entityCenter) {
        Vec3d[] points = new Vec3d[]{
                entityCenter,
                new Vec3d(entityBox.minX, entityBox.minY, entityBox.minZ),
                new Vec3d(entityBox.maxX, entityBox.maxY, entityBox.maxZ)
        };

        for (Vec3d target : points) {
            RaycastContext ctx = new RaycastContext(
                    cameraPos,
                    target,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    client.player
            );
            BlockHitResult result = client.world.raycast(ctx);
            if (result.getType() != HitResult.Type.BLOCK) return true;

            BlockState state = client.world.getBlockState(result.getBlockPos());
            if (!isCullableBlock(state)) return true;
        }
        return false;
    }

    @Unique
    private boolean isCullableBlock(BlockState state) {
        try {
            if (!state.isOpaqueFullCube()) return false;
        } catch (Exception ignored) {
            return false;
        }

        try {
            BlockRenderLayer layer = RenderLayers.getBlockLayer(state);
            return !(layer == BlockRenderLayer.CUTOUT
                    || layer == BlockRenderLayer.CUTOUT_MIPPED
                    || layer == BlockRenderLayer.TRANSLUCENT);
        } catch (Exception ignored) {
            return true;
        }
    }
}
*/
