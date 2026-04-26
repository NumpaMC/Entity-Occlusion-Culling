/*
package yt.Numpamc.frustumcullingentity.mixin;

import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderDispatcherMixin.class)
public class SectionRenderDispatcherMixin {

    @Inject(method = "", at = @At("HEAD"), cancellable = true)
    private void numpamc$cullChunkBeforeRebuild(ChunkSectionPos sectionPos, boolean important, CallbackInfo ci) {
        // Gọi culling check
        if (ChunkCuller.isCullableSection(sectionPos)) {
            // Nếu section bị che kín hoàn toàn → hủy render
            ci.cancel();
        }
    }
}
*/
