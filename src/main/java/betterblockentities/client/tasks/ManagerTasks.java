package betterblockentities.client.tasks;

import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manager work queue "executor" runs each enqueued manager until the manager instance returns FINISHED
 * Each entry is submitted via {@link betterblockentities.client.tasks.ManagerTasks -> schedule } from each
 * block entities animation "trigger"
 */
public final class ManagerTasks {
    private ManagerTasks() {}

    public static final ConcurrentLinkedQueue<InstancedBlockEntityManager> WORK_QUEUE = new ConcurrentLinkedQueue<>();

    public static final int FINISHED = 0;
    public static final int PROCESSING = 1;

    public static void schedule(InstancedBlockEntityManager mgr) {
        if (mgr.tryMarkQueued()) {
            WORK_QUEUE.add(mgr);
        }
    }

    public static void process() {
        int budget = 256;
        while (budget-- > 0) {
            InstancedBlockEntityManager mgr = WORK_QUEUE.poll();
            if (mgr == null) break;

            mgr.clearQueued();

            int state = mgr.run();
            if (state == PROCESSING) {
                if (mgr.isValid()) {
                    schedule(mgr);
                } else {
                    mgr.forceKill();
                }
            }
        }
    }
}

