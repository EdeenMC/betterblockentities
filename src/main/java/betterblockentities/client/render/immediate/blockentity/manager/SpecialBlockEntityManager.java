package betterblockentities.client.render.immediate.blockentity.manager;

import betterblockentities.client.gui.config.ConfigCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Special cases where we might need special behavior : push the render-state at all times etc...
 */
public final class SpecialBlockEntityManager {
    private SpecialBlockEntityManager() {}

    public static boolean shouldRender(BlockEntity blockEntity) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) return true;

        /* check distance to sign from player */
        if (blockEntity instanceof SignBlockEntity) {
            if (!ConfigCache.signText) return false;

            double maxDistSq = (double) ConfigCache.signTextRenderDistance * (double) ConfigCache.signTextRenderDistance;

            var pos = blockEntity.getBlockPos();
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;

            return entity.distanceToSqr(cx, cy, cz) < maxDistSq;
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
}
