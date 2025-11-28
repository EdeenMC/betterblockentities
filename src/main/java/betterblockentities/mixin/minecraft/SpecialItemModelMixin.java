package betterblockentities.mixin.minecraft;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.SpecialItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Supplier;

@Mixin(SpecialItemModel.class)
public class SpecialItemModelMixin<T> {

    @Shadow
    @Final
    private SpecialModelRenderer<T> specialModelType;

    @Unique
    private Vector3f[] cachedVertices;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderState$LayerRenderState;setVertices(Ljava/util/function/Supplier;)V"))
    private void cacheSetVertices(ItemRenderState.LayerRenderState instance, Supplier<Vector3f[]> originalSupplier) {
        instance.setVertices(() -> {
            if (this.cachedVertices == null) {
                Set<Vector3f> set = new ObjectOpenHashSet<>();
                this.specialModelType.collectVertices(set);
                this.cachedVertices = set.toArray(new Vector3f[0]);
            }
            return this.cachedVertices;
        });
    }
}
