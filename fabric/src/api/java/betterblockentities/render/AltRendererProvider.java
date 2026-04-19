package betterblockentities.render;

/* minecraft */
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface AltRendererProvider<T extends BlockEntity, S extends BlockEntityRenderState> {
    AltRenderer<T, S> create(AltRendererProvider.Context context);

    public record Context(
            AltRenderDispatcher blockEntityRenderDispatcher,
            BlockRenderDispatcher blockRenderDispatcher,
            ItemModelResolver itemModelResolver,
            ItemRenderer itemRenderer,
            EntityRenderDispatcher entityRenderer,
            EntityModelSet entityModelSet,
            Font font,
            MaterialSet materials,
            PlayerSkinRenderCache playerSkinRenderCache
    ) {
        public ModelPart bakeLayer(final ModelLayerLocation id) {
            return this.entityModelSet.bakeLayer(id);
        }
    }
}
