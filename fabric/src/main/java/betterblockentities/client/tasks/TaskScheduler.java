package betterblockentities.client.tasks;

/* local */
import betterblockentities.client.BBE;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadInstance;

/* java/misc */
import org.jspecify.annotations.NonNull;

public class TaskScheduler {
    /**
     *  schedules a runnable task on the main minecraft thread
     */
    public static void schedule(@NonNull Runnable task) {
        Minecraft.getInstance().execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                BBE.getLogger().error("Post-reload task failed", t);
            }
        });
    }

    /**
     *  schedules a runnable task on the main minecraft thread after the reload future
     *  has completed and without errors
     */
    public static void scheduleOnReload(ReloadInstance reload, @NonNull Runnable task) {
        if (reload == null) {
            BBE.getLogger().error("Resource reload instance was null! {}", reload);
            return;
        }

        reload.done().whenComplete((ignored, err) -> {
            /* if the reload future completed exceptionally, log and stop */
            if (err != null) {
                BBE.getLogger().error("Resource reload future completed exceptionally", err);
                return;
            }

            /* if reload finished with internal errors, this throws */
            try {
                reload.checkExceptions();
            } catch (Throwable t) {
                BBE.getLogger().error("Resource reload finished with errors", t);
                return;
            }

            /* schedule task on main thread */
            Minecraft.getInstance().execute(() -> {
                try {
                    task.run();
                } catch (Throwable t) {
                    BBE.getLogger().error("Post-reload task failed", t);
                }
            });
        });
    }
}
