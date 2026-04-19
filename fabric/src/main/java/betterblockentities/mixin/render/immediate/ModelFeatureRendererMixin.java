package betterblockentities.mixin.render.immediate;

/* local */
import betterblockentities.client.render.immediate.OverlayRenderer;

/* minecraft */
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* java/misc */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import it.unimi.dsi.fastutil.ints.IntArrays;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
    @Shadow @Final private PoseStack poseStack;

    @Redirect(method = "render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private void fastTranslucentSort(List<SubmitNodeStorage.TranslucentModelSubmit<?>> list, Comparator<?> comparator) {
        int size = list.size();
        if (size < 2)
            return;

        double[] keys = new double[size];
        int[] indices = new int[size];

        for (int i = 0; i < size; i++) {
            keys[i] = -list.get(i).position().lengthSquared();
            indices[i] = i;
        }

        IntArrays.quickSort(indices, (a, b) -> {
            int c = Double.compare(keys[a], keys[b]);
            return c == 0 ? Integer.compare(a, b) : c;
        });

        List<SubmitNodeStorage.TranslucentModelSubmit<?>> sorted = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sorted.add(list.get(indices[i]));
        }

        list.clear();
        list.addAll(sorted);
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V", at = @At("TAIL"))
    public void addRenderers(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource, CallbackInfo ci) {
        OverlayRenderer.renderCrumblingOverlays(crumblingBufferSource, this.poseStack);
    }
}