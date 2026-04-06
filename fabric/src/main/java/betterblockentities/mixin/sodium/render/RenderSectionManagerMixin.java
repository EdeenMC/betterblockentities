package betterblockentities.mixin.sodium.render;

/* local */
import betterblockentities.client.chunk.section.SectionRebuildCallbacks;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderSectionManager.class)
public class RenderSectionManagerMixin {
    @Redirect(method = "uploadChunks", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/BuilderTaskOutput;destroy()V"), remap = false, require = 1)
    private void callback(BuilderTaskOutput out) {
        out.destroy();

        if (SectionRebuildCallbacks.isEmpty()) return;

        RenderSection section = out.render;
        RenderSectionAccessor accessor = (RenderSectionAccessor) section;

        long key = SectionRebuildCallbacks.keyFromSectionPos(accessor.getChunkX(), accessor.getChunkY(), accessor.getChunkZ());
        SectionRebuildCallbacks.fireIfWaiting(key);
    }
}
