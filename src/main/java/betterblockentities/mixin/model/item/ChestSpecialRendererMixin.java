package betterblockentities.mixin.model.item;

/* local */
import betterblockentities.client.gui.config.ConfigCache;

/* minecraft */
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
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
    @Shadow @Final private SpriteGetter sprites;
    @Shadow @Final public static MultiblockChestResources<Identifier> CHRISTMAS;

    /**
     * This will have to be rewritten to account for the spriteId and map that with spriteGetter
     * As for mixin semantics @ModifyArg is no longer possible here
     */
    /*
    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index = 7)
    public SpriteGetter submit(SpriteGetter original) {
        Identifier orgContentsName = original.
        String path = orgContentsName.getPath();

        if (path.contains("normal") || path.contains("trapped")) {
            if (SpecialDates.isExtendedChristmas())
                return original;
            else if (ConfigCache.masterOptimize && ConfigCache.optimizeChests && ConfigCache.christmasChests) {
                Material material = Sheets.CHEST_MAPPER.apply();
                return this.materials.get(material);
            }
        }
        return original;
    }
     */
}