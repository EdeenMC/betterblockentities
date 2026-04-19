package betterblockentities.mixin.render.texture;

/* minecraft */
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* java/misc */
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {
    /* skips checking all entity textures for duplicate atlas entries */
    @Redirect(method = "updateSpriteMaps", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void cancelForEach(Map<Material, TextureAtlasSprite> instance, BiConsumer<Material, TextureAtlasSprite> consumer) {
        instance.forEach((material, sprite) -> {
            Identifier tex = material.texture();
            if (tex != null && tex.getPath().startsWith("entity/")) return;
            consumer.accept(material, sprite);
        });
    }
}
