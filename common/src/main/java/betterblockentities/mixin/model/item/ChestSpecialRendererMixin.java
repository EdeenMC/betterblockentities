package betterblockentities.mixin.model.item;

/* local */
import betterblockentities.client.gui.config.ConfigCache;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.SpecialDates;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChestSpecialRenderer.class)
public class ChestSpecialRendererMixin {
    @Shadow @Final public static MultiblockChestResources<Identifier> CHRISTMAS;

    @Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;I)V"))
    public <S> void bbe$submit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int i3, SpriteId spriteId, SpriteGetter spriteGetter, int outlineColor) {
        Identifier orgContentsName = spriteId.texture();
        String path = orgContentsName.getPath();

        if (path.contains("normal") || path.contains("trapped")) {
            if (SpecialDates.isExtendedChristmas()) {
                collector.submitModel(model, state, poseStack, light, overlayCoords, -1, spriteId, spriteGetter, outlineColor);
                return;
            }
            else if (ConfigCache.optimizeChests && ConfigCache.christmasChests) {
                SpriteId christmasId = Sheets.CHEST_MAPPER.apply(CHRISTMAS.single());
                collector.submitModel(model, state, poseStack, light, overlayCoords, -1, christmasId, spriteGetter, outlineColor);
                return;
            }
        }
        collector.submitModel(model, state, poseStack, light, overlayCoords, -1, spriteId, spriteGetter, outlineColor);
    }
}