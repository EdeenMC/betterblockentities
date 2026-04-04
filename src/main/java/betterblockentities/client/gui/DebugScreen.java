package betterblockentities.client.gui;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.tasks.ManagerTasks;

/* minecraft */
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/* java/misc */
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DebugScreen implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        debugScreenDisplayer.addToGroup(Identifier.fromNamespaceAndPath("bbe", "work_queue"), "[BBE] Manager Work Queue: " + String.valueOf(ManagerTasks.WORK_QUEUE.size()));
    }

    @Override public boolean isAllowed(boolean bl) {
        return true;
    }
    @Override public @NonNull DebugEntryCategory category() {
        return BBE.GlobalScope.DEBUG_CATEGORY;
    }
}
