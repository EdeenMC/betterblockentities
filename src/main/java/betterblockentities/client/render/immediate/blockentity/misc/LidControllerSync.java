package betterblockentities.client.render.immediate.blockentity.misc;

/* local */
import betterblockentities.client.chunk.section.SectionUpdateDispatcher;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.util.BlockVisibilityChecker;
import betterblockentities.mixin.render.immediate.blockentity.chest.ChestBlockEntityAccessor;
import betterblockentities.mixin.render.immediate.blockentity.chest.ChestLidControllerAccessor;

/* minecraft */
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;

public class LidControllerSync {
    public static void sync(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {
        if (!ConfigCache.masterOptimize || !ConfigCache.optimizeChests || !ConfigCache.chestAnims || !clientLevel.isClientSide())
            return;

        final Block block = blockState.getBlock();
        if (!(block instanceof ChestBlock))
            return;

        BlockEntity blockEntity = tryGetBlockEntity(clientLevel, blockPos);
        ChestBlockEntity opposite = BlockVisibilityChecker.getOtherChestHalf(clientLevel, blockPos);

        if (blockEntity == null || opposite == null || !(opposite.getOpenNess(0.5f) > 0f)) {
            return;
        }

        /* sync over the lid controller from the animating half. this also auto schedules a manager for us */
        ChestLidController src = ((ChestBlockEntityAccessor)opposite).getLidController();
        ChestLidController dst = ((ChestBlockEntityAccessor)blockEntity).getLidController();

        ChestLidControllerAccessor accSrc = (ChestLidControllerAccessor)src;
        ChestLidControllerAccessor accDst = (ChestLidControllerAccessor)dst;

        accDst.setOpen(accSrc.getOpen());
        accDst.setProgress(accSrc.getProgress());
        accDst.setLastProgress(accSrc.getLastProgress());

        /* sketchy, force a triggerEvent to wake up the block entity ticker (lithium workaround) */
        clientLevel.blockEvent(blockPos, blockState.getBlock(), 1, 0);

        /* remove this block entity from terrain and switch to immediate rendering */
        BlockEntityExt oppositeExt = (BlockEntityExt)opposite;
        if (oppositeExt.renderingMode() == RenderingMode.IMMEDIATE) {
            BlockEntityExt blockEntityExt = (BlockEntityExt)blockEntity;
            blockEntityExt.terrainMeshReady(false);
            blockEntityExt.renderingMode(RenderingMode.IMMEDIATE);
            SectionUpdateDispatcher.queueRebuildAtBlockPos(blockPos);
        }
    }

    private static BlockEntity tryGetBlockEntity(Level level, BlockPos pos) {
        try {
            return level.getBlockEntity(pos);
        } catch (Exception e) {
            return null;
        }
    }
}
