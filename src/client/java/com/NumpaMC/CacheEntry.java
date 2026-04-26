package com.NumpaMC;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CacheEntry {
    public final boolean visible;
    public final long lastCheck;
    public final Vec3d lastPos;
    public final Box lastBox;

    public CacheEntry(boolean visible, long lastCheck, Vec3d lastPos, Box lastBox) {
        this.visible = visible;
        this.lastCheck = lastCheck;
        this.lastPos = lastPos;
        this.lastBox = lastBox;
    }
}
