package betterblockentities.render;

/* local */
import betterblockentities.data.RegistrationInfo;

/* minecraft */
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* java/misc */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import com.google.common.collect.ImmutableMap;

public class AltRenderDispatcher implements ResourceManagerReloadListener {
    private Map<BlockEntityType<?>, List<AltRenderer<?, ?>>> renderers = ImmutableMap.of();
    private final Map<BlockEntityRenderState, AltRenderer<?, ?>> stateRendererPairs = new HashMap<>();

    private final Font font;
    private final Supplier<EntityModelSet> entityModelSet;
    private Vec3 cameraPos;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;
    private final MaterialSet materials;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public AltRenderDispatcher(
            Font font,
            Supplier<EntityModelSet> supplier,
            BlockRenderDispatcher blockRenderDispatcher,
            ItemModelResolver itemModelResolver,
            ItemRenderer itemRenderer,
            EntityRenderDispatcher entityRenderDispatcher,
            MaterialSet materialSet,
            PlayerSkinRenderCache playerSkinRenderCache
    ) {
        this.itemRenderer = itemRenderer;
        this.itemModelResolver = itemModelResolver;
        this.entityRenderer = entityRenderDispatcher;
        this.font = font;
        this.entityModelSet = supplier;
        this.blockRenderDispatcher = blockRenderDispatcher;
        this.materials = materialSet;
        this.playerSkinRenderCache = playerSkinRenderCache;
    }

    @SuppressWarnings("unchecked")
    public <E extends BlockEntity, S extends BlockEntityRenderState> List<AltRenderer<E, S>> getRenderers(final E blockEntity) {
        return (List<AltRenderer<E, S>>) (List<?>) this.renderers.getOrDefault(blockEntity.getType(), List.of());
    }

    @SuppressWarnings("unchecked")
    public <S extends BlockEntityRenderState> List<AltRenderer<?, S>> getRenderers(final S state) {
        return (List<AltRenderer<?, S>>) (List<?>) this.renderers.getOrDefault(state.blockEntityType, List.of());
    }

    public void prepare(final Vec3 cameraPos) {
        this.cameraPos = cameraPos;
    }

    /* no logging in the hotpath, we just throw, an error here is unexpected and the implementing dev is not at fault most likely */
    public <E extends BlockEntity, S extends BlockEntityRenderState> List<S> tryExtractRenderStates(final E blockEntity, final float partialTicks, final @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        List<AltRenderer<E, S>> renderers = this.getRenderers(blockEntity);

        if (renderers.isEmpty()) {
            return List.of();
        } else if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return List.of();
        }

        Vec3 cameraPosition = this.cameraPos;
        List<S> states = new ArrayList<>();

        for (AltRenderer<E, S> renderer : renderers) {
            RegistrationInfo regInfo = AltRenderers.forRenderer(renderer);
            if (regInfo == null) {
                throw new RuntimeException("RegistrationInfo for a registered AltRenderer was null!");
            }

            if (!renderer.shouldRender(blockEntity, this.cameraPos)) {
                continue;
            }

            S state = renderer.createRenderState();

            addStateRendererPair(state, renderer);

            renderer.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
            states.add(state);
        }
        return states;
    }

    /* no logging in the hotpath, we just throw, an error here is unexpected and the implementing dev is not at fault most likely */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <S extends BlockEntityRenderState> void submit(final S state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        AltRenderer renderer = stateRendererPairs.get(state);

        if (renderer == null) {
            throw new RuntimeException("Could not map this BlockEntityRenderState to a registered AltRenderer -> " + state);
        }

        try {
            renderer.submit(state, poseStack, submitNodeCollector, camera);
        } catch (Exception e) {
            throw new RuntimeException("An exception was caught inside a registered AltRenderer -> ", e);
        }
    }

    private <E extends BlockEntity, S extends BlockEntityRenderState> void addStateRendererPair(S state, AltRenderer<E, S> renderer) {
        stateRendererPairs.put(state, renderer);
    }

    public  void clearStateRendererPairs() {
        stateRendererPairs.clear();
    }

    @Override
    public void onResourceManagerReload(final @NonNull ResourceManager resourceManager) {
        AltRendererProvider.Context context = new AltRendererProvider.Context(
                this,
                this.blockRenderDispatcher,
                this.itemModelResolver,
                this.itemRenderer,
                this.entityRenderer,
                this.entityModelSet.get(),
                this.font,
                this.materials,
                this.playerSkinRenderCache
        );
        this.renderers = AltRenderers.createAltEntityRenderers(context);
    }
}
