package com.NumpaMC.mixin;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.NumpaMC.CacheEntry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Unique private static final double MAX_RENDER_DISTANCE = 110.0;
    @Unique private static final double NEAR_BLOCK_DISTANCE = 1.0;
    @Unique private static final long CACHE_DURATION_MS = 300L;

    @Unique private final Map<T, CacheEntry> visibilityCache = new WeakHashMap<>();
    @Unique private final Map<LivingEntityRenderState, T> stateToEntity = new WeakHashMap<>();

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("HEAD")
    )
    private void onUpdateRenderState(T livingEntity,
                                     LivingEntityRenderState livingEntityRenderState,
                                     float f,
                                     CallbackInfo ci) {
        if (livingEntity == null) return;

        stateToEntity.put(livingEntityRenderState, livingEntity);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null || client.world == null) return;

        Box box = livingEntity.getBoundingBox();
        Vec3d center = box.getCenter();
        Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();
        double distSq = camPos.squaredDistanceTo(center);

        if (distSq > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            visibilityCache.put(livingEntity, new CacheEntry(false, System.currentTimeMillis(), center, box));
            return;
        }

        CacheEntry cached = visibilityCache.get(livingEntity);
        long now = System.currentTimeMillis();
        if (isCacheValid(cached, center, box, now)) return;

        boolean visible = computeVisibility(client, camPos, box);
        visibilityCache.put(livingEntity, new CacheEntry(visible, now, center, box));
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preRender(LivingEntityRenderState livingEntityRenderState,
                           MatrixStack matrixStack,
                           OrderedRenderCommandQueue orderedRenderCommandQueue,
                           CameraRenderState cameraRenderState,
                           CallbackInfo ci) {

        T livingEntity = stateToEntity.get(livingEntityRenderState);
        if (livingEntity == null) {
            livingEntity = extractEntityFromState(livingEntityRenderState);
            if (livingEntity == null) return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null || client.world == null) return;

        Box box = livingEntity.getBoundingBox();
        Vec3d center = box.getCenter();
        CacheEntry cached = visibilityCache.get(livingEntity);
        long now = System.currentTimeMillis();

        if (isCacheValid(cached, center, box, now)) {
            if (!cached.visible) ci.cancel();
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getCameraPos();
        double distSq = camPos.squaredDistanceTo(center);

        if (distSq > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            visibilityCache.put(livingEntity, new CacheEntry(false, now, center, box));
            ci.cancel();
            return;
        }

        boolean visible = computeVisibility(client, camPos, box);
        visibilityCache.put(livingEntity, new CacheEntry(visible, now, center, box));
        if (!visible) ci.cancel();
    }

    @Unique
    private boolean computeVisibility(MinecraftClient client, Vec3d camPos, Box box) {
        if (client.player == null || client.world == null) return true;

        double[] heights = new double[] { box.minY + 0.05, box.getCenter().y, box.maxY - 0.05 };

        for (double y : heights) {
            Vec3d[] points = new Vec3d[] {
                    new Vec3d(box.getCenter().x, y, box.getCenter().z),
                    new Vec3d(box.minX, y, box.minZ),
                    new Vec3d(box.maxX, y, box.maxZ)
            };

            for (Vec3d target : points) {
                RaycastContext ctx = new RaycastContext(
                        camPos,
                        target,
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        client.player
                );

                HitResult hr = client.world.raycast(ctx);
                if (hr == null || hr.getType() != HitResult.Type.BLOCK) return true;

                if (hr instanceof BlockHitResult bhr) {
                    BlockState state = client.world.getBlockState(bhr.getBlockPos());
                    if (!isCullableBlock(state)) return true;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    @Unique
    private boolean isCullableBlock(BlockState state) {
        if (state == null) return false;

        try {
            if (state.getRenderType() != BlockRenderType.MODEL) {
                return false;
            }
        } catch (Throwable ignored) {
            return false;
        }

        try {
            return state.isOpaqueFullCube();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Unique
    private boolean isCacheValid(CacheEntry cached, Vec3d center, Box box, long now) {
        if (cached == null) return false;

        double moved = cached.lastPos == null ? Double.MAX_VALUE : cached.lastPos.squaredDistanceTo(center);
        double boxChanged = cached.lastBox == null ? Double.MAX_VALUE :
                Math.abs(cached.lastBox.getLengthX() - box.getLengthX()) +
                        Math.abs(cached.lastBox.getLengthY() - box.getLengthY()) +
                        Math.abs(cached.lastBox.getLengthZ() - box.getLengthZ());

        return (now - cached.lastCheck < CACHE_DURATION_MS)
                && moved < NEAR_BLOCK_DISTANCE
                && boxChanged < 0.01;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private T extractEntityFromState(LivingEntityRenderState state) {
        if (state == null) return null;

        String[] getters = {"getLivingEntity", "getEntity", "getOwner", "getRenderedEntity"};
        for (String g : getters) {
            try {
                Method m = state.getClass().getMethod(g);
                m.setAccessible(true);
                Object o = m.invoke(state);
                if (o instanceof LivingEntity) return (T) o;
            } catch (Exception ignored) {}
        }

        String[] fields = {"livingEntity", "entity", "owner", "renderedEntity"};
        for (String fName : fields) {
            try {
                Field f = state.getClass().getDeclaredField(fName);
                f.setAccessible(true);
                Object o = f.get(state);
                if (o instanceof LivingEntity) return (T) o;
            } catch (Exception ignored) {}
        }

        try {
            for (Field f : state.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object o = f.get(state);
                if (o instanceof LivingEntity) return (T) o;
            }
        } catch (Exception ignored) {}

        return null;
    }
}