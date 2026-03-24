package betterblockentities.mixin.render.immediate.blockentity.coppergolem;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.CopperGolemStatueBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.CopperGolemStatueRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopperGolemStatueBlockRenderer.class)
public class CopperGolemStatueBlockRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/CopperGolemStatueBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/CopperGolemStatueRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), cancellable = true)
    public void extractRenderState(CopperGolemStatueBlockEntity copperGolemStatueBlockEntity, CopperGolemStatueRenderState copperGolemStatueRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)copperGolemStatueRenderState;
        stateExt.blockEntity(copperGolemStatueBlockEntity);
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/CopperGolemStatueRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public <S> void manageSubmit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, Identifier texture, int light, int overlayCoords, int tint, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, @Local(index = 1)CopperGolemStatueRenderState copperGolemStatueRenderState) {
        if (!ConfigCache.optimizeCopperGolemStatue || !ConfigCache.masterOptimize) {
            collector.submitModel(model, state, poseStack, texture, light, overlayCoords, tint, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)copperGolemStatueRenderState;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
        if (!managed) {
            collector.submitModel(model, state, poseStack, texture, light, overlayCoords, tint, crumblingOverlay);
        }
    }
}
