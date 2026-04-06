package betterblockentities.mixin.resource;

/* local */
import betterblockentities.client.tasks.TaskScheduler;
import betterblockentities.client.tasks.ResourceTasks;

/* minecraft */
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks ReloadableResourceManager#createReload(...) and runs our model baking code
 * AFTER the returned ReloadInstance finishes successfully, doing model baking async
 * can sometimes fail, so we schedule the work onto the Minecraft main client thread
 * to be safe
 */
@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {
    @Inject(method = "createReload", at = @At("RETURN"))
    private void schedulePostReloadTasks(CallbackInfoReturnable<ReloadInstance> cir) {
        ReloadInstance reload = cir.getReturnValue();
        TaskScheduler.scheduleOnReload(reload, () -> {
            if (ResourceTasks.populateGeometryRegistry() == ResourceTasks.FAILED) {
                throw new IllegalStateException("Geometry registry failed to populate");
            }
        });
    }
}
