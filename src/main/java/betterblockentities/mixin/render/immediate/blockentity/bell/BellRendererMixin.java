package betterblockentities.mixin.render.immediate.blockentity.bell;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mojang */
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellRenderer.class)
public class BellRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BellBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BellRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), cancellable = true)
    public void extractRenderState(BellBlockEntity bellBlockEntity, BellRenderState bellRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bellRenderState;
        stateExt.blockEntity(bellBlockEntity);
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BellRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public <S> void manageSubmit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int tint, SpriteId spriteId, SpriteGetter spriteGetter, int i4, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, @Local(index = 1) BellRenderState bellRenderState) {
        if (!ConfigCache.optimizeBells || !ConfigCache.masterOptimize) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bellRenderState;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
        if (!managed) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
        }
    }
}
