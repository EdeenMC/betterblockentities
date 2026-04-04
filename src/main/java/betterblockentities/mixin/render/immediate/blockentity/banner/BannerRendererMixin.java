package betterblockentities.mixin.render.immediate.blockentity.banner;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {
    @Unique private static final ThreadLocal<BannerRenderState> RENDER_STATE = new ThreadLocal<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), cancellable = true)
    public void extractRenderState(BannerBlockEntity bannerBlockEntity, BannerRenderState bannerRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bannerRenderState;
        stateExt.blockEntity(bannerBlockEntity);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void storeState(BannerRenderState bannerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        RENDER_STATE.set(bannerRenderState);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    public void clearState(CallbackInfo ci) {
        RENDER_STATE.remove();
    }

    @Redirect(method = "submitBanner(Lnet/minecraft/client/resources/model/sprite/SpriteGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/client/model/object/banner/BannerModel;Lnet/minecraft/client/model/object/banner/BannerFlagModel;FLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private static <S> void manageSubmitBase(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int tint, SpriteId spriteId, SpriteGetter spriteGetter, int i4, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!ConfigCache.optimizeBanners) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)RENDER_STATE.get();

        boolean managed = stateExt != null && OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, null, light, overlayCoords, tint, crumblingOverlay);
        if (!managed) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
        }
    }

    @Redirect(method = "submitBanner(Lnet/minecraft/client/resources/model/sprite/SpriteGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/client/model/object/banner/BannerModel;Lnet/minecraft/client/model/object/banner/BannerFlagModel;FLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/blockentity/BannerRenderer.submitPatterns (Lnet/minecraft/client/resources/model/sprite/SpriteGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/client/model/Model;Ljava/lang/Object;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private static <S> void manageSubmitCanvas(SpriteGetter spriteGetter, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlayCoords, Model<S> model, S state, boolean bl, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!ConfigCache.optimizeBanners) {
            BannerRendererAccessor.invokeSubmitPatterns(spriteGetter, poseStack, collector, light, overlayCoords, model, state, bl, dyeColor, bannerPatternLayers, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)RENDER_STATE.get();

        float step = -0.45f;
        float rot = step * ConfigCache.bannerPose;
        float rotClamped = Math.clamp(rot, -4.05f, -0.45f);

        model.root().getChild("flag").xRot = (float)Math.toRadians(rotClamped);

        boolean managed = stateExt != null && OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, null, light, overlayCoords, -1, crumblingOverlay);
        if (!managed) {
            BannerRendererAccessor.invokeSubmitPatterns(spriteGetter, poseStack, collector, light, overlayCoords, model, state, bl, dyeColor, bannerPatternLayers, crumblingOverlay);
        }
    }
}
