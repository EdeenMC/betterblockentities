package betterblockentities.mixin.render.immediate.blockentity.sign;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;

/* minecraft */
import betterblockentities.mixin.render.immediate.BlockEntityRenderStateAccessor;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin<S extends SignRenderState> implements BlockEntityRenderer<SignBlockEntity, S> {
    @Shadow protected abstract void submitSignText(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText);
    @Shadow protected abstract Model.Simple getSignModel(S state);

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void manageSubmit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (!ConfigCache.masterOptimize || !ConfigCache.optimizeSigns) return;

        ci.cancel();

        final BlockState bs = ((BlockEntityRenderStateAccessor)state).getBlockState();
        final SignBlock signBlock = (SignBlock)bs.getBlock();

        manageCrumblingOverlay(state, poseStack);
        renderCulledText(state, cameraRenderState, bs, signBlock, poseStack, submitNodeCollector);
    }

    @Unique
    private void manageCrumblingOverlay(S state, PoseStack poseStack) {
        if (state.breakProgress == null) return;

        final Model.Simple model = this.getSignModel(state);

        poseStack.pushPose();
        poseStack.mulPose(state.transformations.body());

        OverlayRenderer.submitCrumblingOverlay(
                poseStack, model, Unit.INSTANCE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                state.breakProgress
        );

        poseStack.popPose();
    }

    @Unique
    private void renderCulledText(S state, CameraRenderState cameraRenderState, BlockState bs, SignBlock signBlock, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!ConfigCache.signTextCulling) {
            poseStack.pushPose();
            poseStack.mulPose(state.transformations.body());

            this.submitSignText(state, poseStack, collector, state.frontText);
            this.submitSignText(state, poseStack, collector, state.backText);

            poseStack.popPose();
            return;
        }

        /* remove text filtering as it adds a bit of overhead */
        final boolean hasFront = hasAnyText(state.frontText, false);
        final boolean hasBack  = hasAnyText(state.backText, false);
        if (!hasFront && !hasBack) return;

        final BlockPos bp = state.blockPos;
        final Vec3 camPos = cameraRenderState.pos;

        final Vec3 off = signBlock.getSignHitboxCenterPosition(bs);
        final double sx = bp.getX() + off.x;
        final double sz = bp.getZ() + off.z;

        /* vector from sign center to camera (XZ only) */
        final double dx = camPos.x - sx;
        final double dz = camPos.z - sz;

        /* fast side test: dot(frontNormal, toCam) > 0, front normal is derived from the sign's yaw degrees */
        final double rotRad = signBlock.getYRotationDegrees(bs) * (Math.PI / 180.0);
        final double nx = -Math.sin(rotRad);
        final double nz =  Math.cos(rotRad);

        /* small epsilon, reduces flicker */
        final boolean camFront = (nx * dx + nz * dz) > 1e-3;

        final boolean drawFront = hasFront && camFront;
        final boolean drawBack  = hasBack  && !camFront;

        /* if the visible side has no text, skip */
        if (!drawFront && !drawBack) return;

        poseStack.pushPose();
        poseStack.mulPose(state.transformations.body());

        if (drawFront) this.submitSignText(state, poseStack, collector, state.frontText);
        if (drawBack)  this.submitSignText(state, poseStack, collector, state.backText);

        poseStack.popPose();
    }

    @Unique
    private static boolean hasAnyText(SignText text, boolean filtered) {
        if (text == null) return false;
        Component[] lines = text.getMessages(filtered);
        for (int i = 0; i < 4; i++) {
            if (!lines[i].getString().isEmpty()) return true;
        }
        return false;
    }
}
