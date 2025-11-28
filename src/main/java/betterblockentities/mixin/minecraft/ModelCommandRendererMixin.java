package betterblockentities.mixin.minecraft;

import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(ModelCommandRenderer.class)
public class ModelCommandRendererMixin {

    @Redirect(method = "render(Lnet/minecraft/client/render/command/BatchingRenderCommandQueue;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/OutlineVertexConsumerProvider;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private void arrayIntSort(List<OrderedRenderCommandQueueImpl.BlendedModelCommand<?>> list,
            Comparator<?> comparator) {
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

        List<OrderedRenderCommandQueueImpl.BlendedModelCommand<?>> sorted = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sorted.add(list.get(indices[i]));
        }

        list.clear();
        list.addAll(sorted);
    }
}
