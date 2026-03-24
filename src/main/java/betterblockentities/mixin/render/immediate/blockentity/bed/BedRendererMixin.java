package betterblockentities.mixin.render.immediate.blockentity.bed;

/* mojang */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedRenderer.class)
public class BedRendererMixin {
    @Unique private static final ThreadLocal<BedRenderState> RENDER_STATE = new ThreadLocal<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BedBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BedRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), cancellable = true)
    public void extractRenderState(BedBlockEntity bedBlockEntity, BedRenderState bedRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bedRenderState;
        stateExt.blockEntity(bedBlockEntity);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BedRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void storeState(BedRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        RENDER_STATE.set(state);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BedRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    private void clearState(CallbackInfo ci) {
        RENDER_STATE.remove();
    }

    @Redirect(method = "submitPiece(Lnet/minecraft/world/level/block/state/properties/BedPart;Lnet/minecraft/client/resources/model/sprite/SpriteId;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public <S> void manageSubmit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int tint, SpriteId spriteId, SpriteGetter spriteGetter, int i4, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!ConfigCache.optimizeBeds || !ConfigCache.masterOptimize) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)RENDER_STATE.get();

        boolean managed = stateExt != null && OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
        if (!managed) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
        }
    }
}
