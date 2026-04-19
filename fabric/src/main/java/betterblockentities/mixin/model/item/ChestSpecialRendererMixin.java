package betterblockentities.mixin.model.item;

/* local */
import betterblockentities.client.gui.config.ConfigCache;

/* minecraft */
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.SpecialDates;

/* mixin */
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChestSpecialRenderer.class)
public class ChestSpecialRendererMixin {
    @Shadow @Final public static Identifier GIFT_CHEST_TEXTURE;
    @Shadow @Final private MaterialSet materials;

    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index = 7)
    public TextureAtlasSprite submit(TextureAtlasSprite original) {
        Identifier orgContentsName = original.contents().name();
        String path = orgContentsName.getPath();

        if (path.contains("normal") || path.contains("trapped")) {
            if (SpecialDates.isExtendedChristmas())
                return original;
            else if (ConfigCache.optimizeChests && ConfigCache.christmasChests) {
                Material material = Sheets.CHEST_MAPPER.apply(GIFT_CHEST_TEXTURE);
                return this.materials.get(material);
            }
        }
        return original;
    }
}