package betterblockentities.client.chunk.section;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/* java/misc */
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SectionRebuildCallbacks {
    private SectionRebuildCallbacks() {}

    private static final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Runnable>> waiting = new ConcurrentHashMap<>();

    public static long keyFromBlockPos(BlockPos pos) {
        int sectionX = pos.getX() >> 4;
        int sectionY = pos.getY() >> 4;
        int sectionZ = pos.getZ() >> 4;
        return keyFromSectionPos(sectionX, sectionY, sectionZ);
    }

    public static long keyFromSectionPos(int sectionX, int sectionY, int sectionZ) {
        return (((long) sectionX & 0x3FFFFF) << 42)
                | (((long) sectionY & 0xFFFFF)  << 22)
                |  ((long) sectionZ & 0x3FFFFF);
    }

    public static void await(BlockPos pos, Runnable runnable) {
        long key = keyFromBlockPos(pos);
        waiting.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(runnable);
    }

    public static void remove(BlockPos pos) {
        long key = keyFromBlockPos(pos);
        waiting.remove(key);
    }

    public static boolean isEmpty() {
        return waiting.isEmpty();
    }

    /** Runs all fence callbacks waiting on this section key */
    public static void fireIfWaiting(long key) {
        ConcurrentLinkedQueue<Runnable> queue = waiting.remove(key);
        if (queue == null) return;

        /* drain quickly */
        ArrayList<Runnable> work = new ArrayList<>();
        for (Runnable runnable; (runnable = queue.poll()) != null;) {
            work.add(runnable);
        }

        /* fail-safe, sub-schedule to main thread */
        Minecraft.getInstance().execute(() -> {
            for (Runnable runnable : work) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    // TODO: log properly
                    t.printStackTrace();
                }
            }
        });
    }
}
