package betterblockentities.mixin.render.immediate.blockentity.banner;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BannerRenderer.class)
public interface BannerRendererAccessor {
    @Invoker("submitPatterns")
    static <S> void invokeSubmitPatterns(
            SpriteGetter sprites,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            Model<S> model,
            S state,
            boolean banner,
            DyeColor baseColor,
            BannerPatternLayers patterns,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        throw new AssertionError();
    }
}
