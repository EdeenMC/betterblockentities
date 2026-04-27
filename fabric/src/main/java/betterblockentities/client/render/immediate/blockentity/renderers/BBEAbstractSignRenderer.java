package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.manager.SpecialBlockEntityManager;
import betterblockentities.mixin.render.immediate.blockentity.BlockEntityRenderStateAccessor;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
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

public abstract class BBEAbstractSignRenderer<S extends SignRenderState> implements BlockEntityRenderer<SignBlockEntity, S> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;
    private final SpriteGetter sprites;

    public BBEAbstractSignRenderer(final BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.sprites = context.sprites();
    }

    protected abstract Model.Simple getSignModel(S state);

    protected abstract SpriteId getSignSprite(WoodType type);

    public void submit(final S state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState cameraRenderState) {
        final BlockState bs = ((BlockEntityRenderStateAccessor)state).getBlockState();
        final SignBlock signBlock = (SignBlock)bs.getBlock();

        if (!BBE.GlobalScope.limitVanillaSignRendering) {
            Model.Simple bodyModel = this.getSignModel(state);

            poseStack.pushPose();
            poseStack.mulPose(state.transformations.body());
            this.submitSign(poseStack, state.lightCoords, signBlock.type(), bodyModel, state.breakProgress, submitNodeCollector);
            poseStack.popPose();
        }
        manageCrumblingOverlay(state, poseStack);
        renderCulledText(state, cameraRenderState, bs, signBlock, poseStack, submitNodeCollector);
    }

    @Unique
    protected void submitSign(final PoseStack poseStack, final int lightCoords, final WoodType type, final Model.Simple signModel, final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, final SubmitNodeCollector submitNodeCollector) {
        SpriteId sprite = this.getSignSprite(type);
        submitNodeCollector.submitModel(signModel, Unit.INSTANCE, poseStack, lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, this.sprites, 0, breakProgress);
    }

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

    private void renderCulledText(S state, CameraRenderState cameraRenderState, BlockState bs, SignBlock signBlock, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!ConfigCache.signTextCulling) {
            poseStack.pushPose();
            poseStack.mulPose(state.transformations.frontText());
            this.submitSignText(state, poseStack, collector, state.frontText);
            poseStack.popPose();


            poseStack.pushPose();
            poseStack.mulPose(state.transformations.backText());
            this.submitSignText(state, poseStack, collector, state.backText);
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
            poseStack.mulPose(state.transformations.frontText());
            submitSignText(state, poseStack, collector, state.frontText);
            poseStack.popPose();
        }
        if (drawBack)  {
            poseStack.pushPose();
            poseStack.mulPose(state.transformations.backText());
            submitSignText(state, poseStack, collector, state.backText);
            poseStack.popPose();
        }
    }

    private void submitSignText(final S state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final SignText signText) {
        int darkColor = getDarkColor(signText);
        int signMidpoint = 4 * state.textLineHeight / 2;
        FormattedCharSequence[] formattedLines = signText.getRenderMessages(state.isTextFilteringEnabled, input -> {
            List<FormattedCharSequence> components = this.font.split(input, state.maxTextLineWidth);
            return components.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)components.get(0);
        });
        int textColor;
        boolean drawOutline;
        int lightVal;
        if (signText.hasGlowingText()) {
            textColor = signText.getColor().getTextColor();
            drawOutline = textColor == DyeColor.BLACK.getTextColor() || state.drawOutline;
            lightVal = 15728880;
        } else {
            textColor = darkColor;
            drawOutline = false;
            lightVal = state.lightCoords;
        }

        for (int i = 0; i < 4; i++) {
            FormattedCharSequence actualLine = formattedLines[i];
            float x1 = -this.font.width(actualLine) / 2;
            submitNodeCollector.submitText(
                    poseStack,
                    x1,
                    i * state.textLineHeight - signMidpoint,
                    actualLine,
                    false,
                    Font.DisplayMode.POLYGON_OFFSET,
                    lightVal,
                    textColor,
                    0,
                    drawOutline ? darkColor : 0
            );
        }
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

    public void extractRenderState(
            final SignBlockEntity blockEntity,
            final S state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.maxTextLineWidth = blockEntity.getMaxTextLineWidth();
        state.textLineHeight = blockEntity.getTextLineHeight();
        state.frontText = blockEntity.getFrontText();
        state.backText = blockEntity.getBackText();
        state.isTextFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
        state.drawOutline = isOutlineVisible(blockEntity.getBlockPos());
        state.woodType = SignBlock.getWoodType(blockEntity.getBlockState().getBlock());
    }
}
