package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.manager.SpecialBlockEntityManager;
import betterblockentities.mixin.render.immediate.blockentity.BlockEntityRenderStateAccessor;

/* minecraft */
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

/* java/misc */
import java.util.List;
import java.util.Objects;

public abstract class BBEAbstractSignRenderer implements BlockEntityRenderer<SignBlockEntity, SignRenderState> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;
    private final MaterialSet materials;

    public BBEAbstractSignRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.materials = context.materials();
    }

    protected abstract Model.Simple getSignModel(BlockState blockState, WoodType woodType);

    protected abstract Material getSignMaterial(WoodType woodType);

    protected abstract float getSignModelRenderScale();

    protected abstract float getSignTextRenderScale();

    protected abstract Vec3 getTextOffset();

    protected abstract void translateSign(PoseStack poseStack, float f, BlockState blockState);

    public void submit(SignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        final BlockState bs = ((BlockEntityRenderStateAccessor)state).getBlockState();
        final SignBlock signBlock = (SignBlock)bs.getBlock();

        if (!BBE.GlobalScope.limitVanillaSignRendering) {
            Model.Simple simple = this.getSignModel(bs, signBlock.type());

            poseStack.pushPose();
            this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
            this.submitSign(poseStack, state.lightCoords, signBlock.type(), simple, state.breakProgress, submitNodeCollector);
            poseStack.popPose();
        }
        manageCrumblingOverlay(state, poseStack, signBlock, bs);
        renderCulledText(state, cameraRenderState, bs, signBlock, poseStack, submitNodeCollector);
    }

    protected void submitSign(PoseStack poseStack, int i, WoodType woodType, Model.Simple simple, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        float f = this.getSignModelRenderScale();
        poseStack.scale(f, -f, -f);
        Material material = this.getSignMaterial(woodType);
        Objects.requireNonNull(simple);
        RenderType renderType = material.renderType(simple::renderType);
        submitNodeCollector.submitModel(simple, Unit.INSTANCE, poseStack, renderType, i, OverlayTexture.NO_OVERLAY, -1, this.materials.get(material), 0, crumblingOverlay);
        poseStack.popPose();
    }

    private void manageCrumblingOverlay(SignRenderState state, PoseStack poseStack, SignBlock signBlock, BlockState bs) {
        if (state.breakProgress == null) return;

        final Model.Simple model = this.getSignModel(bs, signBlock.type());

        poseStack.pushPose();
        this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
        float f = this.getSignModelRenderScale();
        poseStack.scale(f, -f, -f);

        OverlayRenderer.submitCrumblingOverlay(
                poseStack, model, Unit.INSTANCE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                state.breakProgress
        );

        poseStack.popPose();
    }

    private void renderCulledText(SignRenderState state, CameraRenderState cameraRenderState, BlockState bs, SignBlock signBlock, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!ConfigCache.signTextCulling) {
            poseStack.pushPose();
            this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
            this.submitSignText(state, poseStack, collector, true);
            poseStack.popPose();


            poseStack.pushPose();
            this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
            this.submitSignText(state, poseStack, collector, false);
            poseStack.popPose();
            return;
        }

        /* rerun this check again for modded environments that "skips" our premature check before state creation/extraction */
        final boolean hasFront = SpecialBlockEntityManager.hasAnyText(state.frontText, false);
        final boolean hasBack  = SpecialBlockEntityManager.hasAnyText(state.backText, false);
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

        if (drawFront) {
            poseStack.pushPose();
            this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
            submitSignText(state, poseStack, collector, true);
            poseStack.popPose();
        }
        if (drawBack)  {
            poseStack.pushPose();
            this.translateSign(poseStack, -signBlock.getYRotationDegrees(bs), bs);
            submitSignText(state, poseStack, collector, false);
            poseStack.popPose();
        }
    }

    private void submitSignText(SignRenderState signRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, boolean bl) {
        SignText signText = bl ? signRenderState.frontText : signRenderState.backText;
        if (signText != null) {
            poseStack.pushPose();
            this.translateSignText(poseStack, bl, this.getTextOffset());
            int i = getDarkColor(signText);
            int j = 4 * signRenderState.textLineHeight / 2;
            FormattedCharSequence[] formattedCharSequences = signText.getRenderMessages(signRenderState.isTextFilteringEnabled, (component) -> {
                List<FormattedCharSequence> list = this.font.split(component, signRenderState.maxTextLineWidth);
                return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
            });
            int k;
            boolean bl2;
            int l;
            if (signText.hasGlowingText()) {
                k = signText.getColor().getTextColor();
                bl2 = k == DyeColor.BLACK.getTextColor() || signRenderState.drawOutline;
                l = 15728880;
            } else {
                k = i;
                bl2 = false;
                l = signRenderState.lightCoords;
            }

            for(int m = 0; m < 4; ++m) {
                FormattedCharSequence formattedCharSequence = formattedCharSequences[m];
                float f = (float)(-this.font.width(formattedCharSequence) / 2);
                submitNodeCollector.submitText(poseStack, f, (float)(m * signRenderState.textLineHeight - j), formattedCharSequence, false, Font.DisplayMode.POLYGON_OFFSET, l, k, 0, bl2 ? i : 0);
            }

            poseStack.popPose();
        }
    }

    private void translateSignText(PoseStack poseStack, boolean bl, Vec3 vec3) {
        if (!bl) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }

        float f = 0.015625F * this.getSignTextRenderScale();
        poseStack.translate(vec3);
        poseStack.scale(f, -f, f);
    }

    private static boolean isOutlineVisible(final BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && minecraft.options.getCameraType().isFirstPerson() && player.isScoping()) {
            return true;
        } else {
            Entity camera = minecraft.getCameraEntity();
            return camera != null && camera.distanceToSqr(Vec3.atCenterOf(pos)) < OUTLINE_RENDER_DISTANCE;
        }
    }

    public static int getDarkColor(final SignText signText) {
        int color = signText.getColor().getTextColor();
        return color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText() ? -988212 : ARGB.scaleRGB(color, 0.4F);
    }

    public SignRenderState createRenderState() {
        return new SignRenderState();
    }

    public void extractRenderState(SignBlockEntity signBlockEntity, SignRenderState signRenderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(signBlockEntity, signRenderState, f, vec3, crumblingOverlay);
        signRenderState.maxTextLineWidth = signBlockEntity.getMaxTextLineWidth();
        signRenderState.textLineHeight = signBlockEntity.getTextLineHeight();
        signRenderState.frontText = signBlockEntity.getFrontText();
        signRenderState.backText = signBlockEntity.getBackText();
        signRenderState.isTextFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
        signRenderState.drawOutline = isOutlineVisible(signBlockEntity.getBlockPos());
    }
}
