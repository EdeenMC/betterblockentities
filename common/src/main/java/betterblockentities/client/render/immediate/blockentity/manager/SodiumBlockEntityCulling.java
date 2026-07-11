package betterblockentities.client.render.immediate.blockentity.manager;

/* local */
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;
import betterblockentities.render.AltRenderers;

/* minecraft */
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SodiumBlockEntityCulling {

    public static boolean canRemoveFromImmediateList(BlockEntity blockEntity) {
        if (!(blockEntity instanceof BlockEntityExt ext)) return false;

        return ext.bbe$isSupportedBlockEntity()
                && BBEConfig.OptEnabledTable.ENABLED[ext.bbe$getOptKind() & 0xFF]
                && ext.bbe$getRenderingMode() == RenderingMode.TERRAIN
                && !ext.bbe$hasSpecialManager()
                && !AltRenderers.renderersLoaded();
    }

    public static boolean needsSeparateBreakingState(BlockEntity blockEntity) {
        return canRemoveFromImmediateList(blockEntity) && ((BlockEntityExt) blockEntity).bbe$isTerrainMeshReady();
    }
}
