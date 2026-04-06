package betterblockentities.client.tasks;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.model.geometry.GeometryRegistry;
import betterblockentities.client.model.geometry.ModelGenerator;

/**
 * Tasks to be executed after resource reload, these tasks should be scheduled with
 * {@link betterblockentities.client.tasks.TaskScheduler -> scheduleOnReload }
 */
public class ResourceTasks {
    public static int FAILED = 0xFFFF;
    public static int COMPLETE = 0x0000;

    public static int populateGeometryRegistry() {
        try {
            if (!GeometryRegistry.getCache().isEmpty()) {
                BBE.getLogger().info("Clearing geometry registry!");
                GeometryRegistry.clearCache();
            }
            if (ModelGenerator.generateAppend() == COMPLETE) {
                BBE.getLogger().info("Geometry registry populated! Task successfully completed");
                return COMPLETE;
            }
            else {
                BBE.getLogger().error("Could not prepare the necessary geometry because the entityModelSet was null. Check previous logs!");
                return FAILED;
            }
        } catch (Throwable t) {
            BBE.getLogger().error("Setup/bake task failed! because of internal error", t);
            return FAILED;
        }
    }
}
