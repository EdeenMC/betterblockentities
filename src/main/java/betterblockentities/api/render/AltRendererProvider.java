package betterblockentities.api.render;

/* minecraft */
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface AltRendererProvider<T extends BlockEntity, S extends BlockEntityRenderState> {
    AltRenderer<T, S> create(AltRendererProvider.Context context);

    public record Context(
            AltRenderDispatcher renderDispatcher,
            BlockModelResolver blockModelResolver,
            ItemModelResolver itemModelResolver,
            EntityRenderDispatcher entityRenderer,
            EntityModelSet entityModelSet,
            Font font,
            SpriteGetter sprites,
            PlayerSkinRenderCache playerSkinRenderCache
    ) {
        public ModelPart bakeLayer(final ModelLayerLocation id) {
            return this.entityModelSet.bakeLayer(id);
        }
    }
}
