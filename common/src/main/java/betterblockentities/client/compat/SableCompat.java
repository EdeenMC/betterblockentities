package betterblockentities.client.compat;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Compatibility with Sable (Create: Aeronautics' physics engine).
 *
 * <p>Root cause of the reported bug: Better Block Entities assumes every supported
 * block entity lives in the main {@code ClientLevel} terrain meshed by Sodium.
 * A newly constructed block entity defaults to {@code renderingMode=TERRAIN} with
 * {@code terrainMeshReady=true}, so {@link
 * betterblockentities.mixin.render.immediate.blockentity.BlockEntityRenderDispatcherMixin}
 * immediately culls the vanilla block-entity renderer (BER) and waits for Sodium to
 * mesh the model into the terrain chunk.</p>
 *
 * <p>Sable sublevels (contraptions) are <em>not</em> meshed by Sodium's main-world
 * pipeline. They live in isolated plot chunks (far-away "shadow" positions) and are
 * rendered by Sable's own {@code SubLevelRenderDispatcher}, which collects block
 * entities for vanilla BER rendering and never bakes BBE's static terrain models.
 * The result: a block entity placed on a contraption is culled from the BER path
 * while no terrain mesh ever appears for it, so it stays invisible. Interacting
 * with it (e.g. opening a chest) flips BBE into {@code IMMEDIATE} mode, which
 * re-enables the BER path -- matching the reported "invisible until interacted
 * with" symptom exactly.</p>
 *
 * <p>Fix: detect block entities that live inside a Sable sublevel and keep them on
 * the vanilla BER path permanently -- never cull them, never bake them into
 * Sodium terrain, and never queue main-world section rebuilds for their plot
 * positions.</p>
 *
 * <p>This class uses reflection so Sable remains an <em>optional</em> dependency:
 * when Sable is not installed every check cheaply returns {@code false} and BBE
 * behaves exactly as upstream.</p>
 */
public final class SableCompat {
    private SableCompat() {}

    private static volatile Boolean sableLoaded;
    private static volatile Object sableHelper;
    private static volatile java.lang.reflect.Method getContainingByBlockEntity;

    /**
     * @return true if the Sable mod classes are present on the classpath.
     */
    public static boolean isSableLoaded() {
        Boolean cached = sableLoaded;
        if (cached != null) {
            return cached;
        }
        boolean loaded = false;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            java.lang.reflect.Field helperField = sableClass.getField("HELPER");
            Object helper = helperField.get(null);
            if (helper != null) {
                java.lang.reflect.Method method =
                        helper.getClass().getMethod("getContaining", BlockEntity.class);
                sableHelper = helper;
                getContainingByBlockEntity = method;
                loaded = true;
            }
        } catch (Throwable ignored) {
            loaded = false;
        }
        sableLoaded = loaded;
        return loaded;
    }

    /**
     * Checks whether the given block entity lives inside a Sable sublevel plot.
     * Safe to call from render threads and chunk-meshing worker threads: any
     * failure fails open to {@code false} (upstream BBE behaviour).
     *
     * @param blockEntity the block entity to test (may be null)
     * @return true if Sable reports this block entity as contained in a sublevel
     */
    public static boolean isOnSubLevel(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return false;
        }
        try {
            if (blockEntity.getLevel() == null) {
                return false;
            }
        } catch (Throwable ignored) {
            return false;
        }
        if (!isSableLoaded()) {
            return false;
        }
        try {
            Object helper = sableHelper;
            java.lang.reflect.Method method = getContainingByBlockEntity;
            if (helper == null || method == null) {
                return false;
            }
            Object subLevel = method.invoke(helper, blockEntity);
            return subLevel != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
