package betterblockentities.client.render.immediate.blockentity.manager;

/* local */
import betterblockentities.client.gui.config.ConfigCache;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* java/misc */
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Special cases where we might need special behavior : push the render-state at all times etc...
 */
public final class SpecialBlockEntityManager {
    private SpecialBlockEntityManager() {}

    public static @Nullable BlockEntityRenderState extractManagedState(BlockEntity blockEntity, CameraRenderState cameraRenderState, float partialTicks, ModelFeatureRenderer.CrumblingOverlay breakProgress, boolean isGloballyRendered) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return null;
        }

        if (blockEntity instanceof SignBlockEntity signBlockEntity) {
            return SignManager.extractSignState(
                    cameraEntity,
                    signBlockEntity,
                    cameraRenderState,
                    partialTicks,
                    breakProgress,
                    isGloballyRendered
            );
        }
        else if (blockEntity instanceof ShelfBlockEntity shelfBlockEntity) {
            return ShelfManager.extractShelfState(
                    shelfBlockEntity,
                    cameraRenderState,
                    partialTicks,
                    breakProgress,
                    isGloballyRendered
            );
        }
        else if (blockEntity instanceof LecternBlockEntity lecternBlockEntity) {
            return LecternManager.extractLecternState(
                    lecternBlockEntity,
                    cameraRenderState,
                    partialTicks,
                    breakProgress,
                    isGloballyRendered
            );
        }
        else if (blockEntity instanceof CampfireBlockEntity campfireBlockEntity) {
            return CampfireManager.extractCampfireState(
                    campfireBlockEntity,
                    cameraRenderState,
                    partialTicks,
                    breakProgress,
                    isGloballyRendered
            );
        }
        return null;
    }

    private static class CampfireManager {
        private static @Nullable BlockEntityRenderState extractCampfireState(
                CampfireBlockEntity blockEntity,
                CameraRenderState cameraRenderState,
                float partialTicks,
                ModelFeatureRenderer.CrumblingOverlay breakProgress,
                boolean isGloballyRendered
        ) {
            for (ItemStack stack : blockEntity.getItems()) {
                if (stack != ItemStack.EMPTY) {
                    return Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(
                            blockEntity,
                            partialTicks,
                            breakProgress,
                            isGloballyRendered
                    );
                }
            }
            return null;
        }
    }

    private static class ShelfManager {
        private static @Nullable BlockEntityRenderState extractShelfState(
                ShelfBlockEntity blockEntity,
                CameraRenderState cameraRenderState,
                float partialTicks,
                ModelFeatureRenderer.CrumblingOverlay breakProgress,
                boolean isGloballyRendered
        ) {
            for (ItemStack stack : blockEntity.getItems()) {
                if (stack != ItemStack.EMPTY) {
                    return Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(
                            blockEntity,
                            partialTicks,
                            breakProgress,
                            isGloballyRendered
                    );
                }
            }
            return null;
        }
    }

    private static class LecternManager {
        private static @Nullable BlockEntityRenderState extractLecternState(
                LecternBlockEntity blockEntity,
                CameraRenderState cameraRenderState,
                float partialTicks,
                ModelFeatureRenderer.CrumblingOverlay breakProgress,
                boolean isGloballyRendered
        ) {
            if (blockEntity.getBlockState().getValue(LecternBlock.HAS_BOOK)) {
                return Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(
                        blockEntity,
                        partialTicks,
                        breakProgress,
                        isGloballyRendered
                );
            }
            return null;
        }
    }

    private static class SignManager {
        private static @Nullable BlockEntityRenderState extractSignState(
                Entity cameraEntity,
                SignBlockEntity blockEntity,
                CameraRenderState cameraRenderState,
                float partialTicks,
                ModelFeatureRenderer.CrumblingOverlay breakProgress,
                boolean isGloballyRendered)
        {
            if (!ConfigCache.signText) {
                return null;
            }

            if (!checkDistanceToCamera(cameraEntity, blockEntity)) {
                return null;
            }

            SignRenderState editorState = Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(
                    blockEntity,
                    partialTicks,
                    breakProgress,
                    isGloballyRendered
            );

            if (!ConfigCache.signTextCulling) {
                return editorState;
            }

            boolean hasFront = hasAnyText(blockEntity.getText(SignTextSlot.FRONT), false);
            boolean hasBack  = hasAnyText(blockEntity.getText(SignTextSlot.BACK), false);

            if (!hasFront && !hasBack) {
                return null;
            }

            boolean frontTest = isCameraFrontFacing(blockEntity, cameraRenderState);
            boolean drawFront = hasFront && frontTest;
            boolean drawBack = hasBack && !frontTest;

            /* if both sides are not visible, don't extract this render-state */
            if (!drawFront && !drawBack || editorState == null) {
                return null;
            }

            /* invalidate these accordingly so they don't get rendered */
            if (!drawFront) {
                editorState.frontText = null;
            }

            if (!drawBack) {
                editorState.backText = null;
            }

            return editorState;
        }

        private static boolean isCameraFrontFacing(SignBlockEntity blockEntity, CameraRenderState cameraRenderState) {
            BlockState blockState = blockEntity.getBlockState();
            SignBlock signBlock = (SignBlock)blockState.getBlock();
            BlockPos blockPos = blockEntity.getBlockPos();
            Vec3 camPos = cameraRenderState.pos;

            Vec3 off = signBlock.getSignHitboxCenterPosition(blockState);
            double sx = blockPos.getX() + off.x;
            double sz = blockPos.getZ() + off.z;

            /* vector from sign center to camera (XZ only) */
            double dx = camPos.x - sx;
            double dz = camPos.z - sz;

            /* fast side test: dot(frontNormal, toCam) > 0, front normal is derived from the sign's yaw degrees */
            double rotRad = signBlock.getYRotationDegrees(blockState) * (Math.PI / 180.0);
            double nx = -Math.sin(rotRad);
            final double nz =  Math.cos(rotRad);

            /* small epsilon, reduces flicker */
            return (nx * dx + nz * dz) > 1e-3;
        }

        private static boolean hasAnyText(SignText text, boolean filtered) {
            if (text == null) {
                return false;
            }

            List<Component> lines = text.getMessages(filtered);

            for (Component line : lines) {
                if (!line.getString().isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        private static boolean checkDistanceToCamera(Entity cameraEntity, SignBlockEntity blockEntity) {
            double maxDistSq = (double) ConfigCache.signTextRenderDistance * (double) ConfigCache.signTextRenderDistance;

            var pos = blockEntity.getBlockPos();
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            if (cameraEntity.distanceToSqr(cx, cy, cz) > maxDistSq) {
                return false;
            }

            return true;
        }
    }
}
