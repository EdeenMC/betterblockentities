package betterblockentities.mixin.iris;

/* local */
import betterblockentities.client.compat.ModCompat;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.Iris", remap = false)
public class IrisMixin {
    
    @Inject(method = "onLoadingComplete", at = @At("HEAD"))
    public void bbe$setIrisLoaded(CallbackInfo ci){
        // Iris detection
        ModCompat.setIrisLoaded();
    }
}
