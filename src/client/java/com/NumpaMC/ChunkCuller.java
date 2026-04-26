/*
package yt.Numpamc.frustumcullingentity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public class ChunkCuller {

    private static final long CACHE_DURATION_MS = 500L;
    private static final double MAX_RENDER_DISTANCE = 180.0;
    private static final double HEIGHT = 128.0;

    private static final Map<Long, CacheEntry> cache = new WeakHashMap<>();

    public static boolean isChunkVisible(MinecraftClient client, ChunkPos chunkPos) {
        if (client == null || client.world == null || client.gameRenderer == null) return true;

        World world = client.world;
        Camera cam = client.gameRenderer.getCamera();
        Vec3d camPos = cam.getPos();
        long now = System.currentTimeMillis();

        long key = chunkPos.toLong();
        CacheEntry entry = cache.get(key);

        if (entry != null && now - entry.lastCheck < CACHE_DURATION_MS)
            return entry.visible;

        double cx = chunkPos.getStartX() + 8;
        double cz = chunkPos.getStartZ() + 8;
        Vec3d chunkCenter = new Vec3d(cx, HEIGHT, cz);

        if (camPos.squaredDistanceTo(chunkCenter) > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
            cache.put(key, new CacheEntry(false, now));
            return false;
        }

        boolean visible = rayVisible(world, camPos, chunkCenter);
        cache.put(key, new CacheEntry(visible, now));
        return visible;
    }

    private static boolean rayVisible(World world, Vec3d from, Vec3d to) {
        RaycastContext ctx = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                (Entity) null
        );
        HitResult hr = world.raycast(ctx);

        if (hr == null || hr.getType() != HitResult.Type.BLOCK) return true;

        if (hr instanceof BlockHitResult bhr) {
            BlockPos hitPos = bhr.getBlockPos();
            if (world.getBlockState(hitPos).isOpaqueFullCube()) return false;
        }

        return true;
    }

    private static class CacheEntry {
        final boolean visible;
        final long lastCheck;
        CacheEntry(boolean visible, long lastCheck) {
            this.visible = visible;
            this.lastCheck = lastCheck;
        }
    }
}
*/
