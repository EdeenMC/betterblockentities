package betterblockentities.api.render;

/* local */
import betterblockentities.api.data.RegistrationInfo;
import betterblockentities.api.registration.RegistrationCollection;

/* minecraft */
import net.minecraft.world.level.block.entity.BlockEntityType;

/* java/misc */
import com.google.common.collect.ImmutableMap;
import java.util.*;

public final class AltRenderers {
    private static final Map<AltRenderer<?, ?>, RegistrationInfo> LOADED_ALT_RENDERERS = new IdentityHashMap<>();
    private static final Map<BlockEntityType<?>, Boolean> OVERRIDES = new HashMap<>();

    public static Map<BlockEntityType<?>, List<AltRenderer<?, ?>>> createAltEntityRenderers(final AltRendererProvider.Context context) {
        clear();

        Map<BlockEntityType<?>, List<AltRenderer<?, ?>>> grouped = new HashMap<>();

        for (RegistrationInfo altRendererInfo : RegistrationCollection.getRegistrations().values()) {
            BlockEntityType<?> type = altRendererInfo.blockEntityType().type();
            AltRenderer<?, ?> renderer = altRendererInfo.rendererProvider().create(context);

            if (renderer.dedicatedRenderer()) {
                OVERRIDES.putIfAbsent(type, true);
            }

            LOADED_ALT_RENDERERS.put(renderer, altRendererInfo);
            grouped.computeIfAbsent(type, ignored -> new ArrayList<>()).add(renderer);
        }

        ImmutableMap.Builder<BlockEntityType<?>, List<AltRenderer<?, ?>>> result = ImmutableMap.builder();

        for (Map.Entry<BlockEntityType<?>, List<AltRenderer<?, ?>>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        return result.build();
    }

    public static boolean hasRendererOverride(BlockEntityType<?> blockEntityType) {
        return OVERRIDES.get(blockEntityType) != null;
    }

    public static RegistrationInfo forRenderer(AltRenderer<?, ?> renderer) {
        return LOADED_ALT_RENDERERS.get(renderer);
    }

    public static Map<AltRenderer<?, ?>, RegistrationInfo> getLoadedAltRenderers() {
        return Map.copyOf(LOADED_ALT_RENDERERS);
    }

    private static void clear() {
        LOADED_ALT_RENDERERS.clear();
        OVERRIDES.clear();
    }
}
