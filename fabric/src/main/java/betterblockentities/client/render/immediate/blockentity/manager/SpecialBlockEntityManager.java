package betterblockentities.client.render.immediate.blockentity.manager;

import betterblockentities.client.gui.config.ConfigCache;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Special cases where we might need special behavior : push the render-state at all times etc...
 */
public final class SpecialBlockEntityManager {
    private SpecialBlockEntityManager() {}

    public static boolean shouldRender(BlockEntity blockEntity) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) return true;

        /* check distance to sign from player */
        if (blockEntity instanceof SignBlockEntity sign) {
            if (!ConfigCache.signText) return false;

            double maxDistSq = (double) ConfigCache.signTextRenderDistance * (double) ConfigCache.signTextRenderDistance;

            var pos = blockEntity.getBlockPos();
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            if (entity.distanceToSqr(cx, cy, cz) > maxDistSq) {
                return false;
            }

            SignText frontText = sign.getFrontText();
            SignText backText = sign.getBackText();

            /* prematurely check if the sign has any text at all, if not, don't proceed, we continue to cull each side inside the renderer */
            final boolean hasFront = hasAnyText(frontText, false);
            final boolean hasBack  = hasAnyText(backText, false);
            if (!hasFront && !hasBack) return false;

            return true;
        }

        /* don't continue to extract this render state if we have no items to render */
        else if (blockEntity instanceof ShelfBlockEntity shelf) {
            for (ItemStack stack : shelf.getItems()) {
                if (stack != ItemStack.EMPTY) {
                    return true;
                }
            }
            return false;
        }
        else if (blockEntity instanceof CampfireBlockEntity campfire) {
            for (ItemStack stack : campfire.getItems()) {
                if (stack != ItemStack.EMPTY) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean hasAnyText(SignText text, boolean filtered) {
        if (text == null) return false;
        Component[] lines = text.getMessages(filtered);
        for (int i = 0; i < 4; i++) {
            if (!lines[i].getString().isEmpty()) return true;
        }
        return false;
    }
}
