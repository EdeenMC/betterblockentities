package betterblockentities.client.chunk.section;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.tasks.TaskScheduler;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for executing render section rebuild tasks
 */
public class SectionUpdateDispatcher {
    public static void queueRebuildAtBlockPos(BlockPos pos) {
        try {
            TaskScheduler.schedule(() -> {
                Level level = Minecraft.getInstance().level;
                if (level == null) return;

                BlockState state = level.getBlockState(pos);
                Minecraft.getInstance().levelRenderer.blockChanged(level, pos, state, state, 8);
            });
        } catch (Exception e) {
            BBE.getLogger().error("Failed to rebuild terrain section!", e);
            SectionRebuildCallbacks.remove(pos);
        }
    }

    /**
     * rebuild section with a fence callback (runnable runs after section rebuild is complete)
     */
    public static void queueRebuildAtBlockPos(BlockPos pos, Runnable onUploadedFence) {
        SectionRebuildCallbacks.await(pos, onUploadedFence);
        queueRebuildAtBlockPos(pos);
    }

    public static void queueUpdateAllSections() {
        try {
            Minecraft.getInstance().levelRenderer.allChanged();
        } catch (Exception e) {
            BBE.getLogger().error("Reloading terrain sections failed!", e);
        }
    }
}
