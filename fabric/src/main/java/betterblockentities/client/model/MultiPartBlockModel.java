package betterblockentities.client.model;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.model.geometry.ModelUtility;
import betterblockentities.mixin.model.modelpart.ModelPartAccessor;

/* minecraft */
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.NonNull;

/* java/misc */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultiPartBlockModel implements BlockStateModel {
    private final List<BlockStateModel> models = new ArrayList<>();
    private final Map<String, BlockStateModel> pairs = new HashMap<>();

    public MultiPartBlockModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        generateMeshModel(root, sprite, stack);
    }

    public MultiPartBlockModel(List<BlockStateModelPart> parts) {
        constructSingleVariants(parts);
    }

    private void generateMeshModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        ModelPartAccessor modelAcc = (ModelPartAccessor)(Object)root;
        if (modelAcc == null) {
            BBE.getLogger().error("Failed to invoke accessor on root model part with sprite {}", sprite.contents().name());
            return;
        }

        Map<String, ModelPart> children = modelAcc.getChildren();
        if (children.isEmpty()) {
            BBE.getLogger().error("Root model part with sprite {} has no children, skipping!", sprite.contents().name());
            return;
        }

        children.forEach((key, part) -> {
            List<BakedQuad> quads = new ArrayList<>();
            bakePartToQuads(part, quads, sprite, stack);

            QuadCollection collection = toUnculledCollection(quads);
            SimpleModelWrapper wrapper = new SimpleModelWrapper(collection, true, null);

            constructSingleVariants(List.of(wrapper));
            createModelPairs(key);
        });
    }

    /**
     * if there are any nested children in this part, this should traverse all of them
     */
    private void bakePartToQuads(ModelPart part, List<BakedQuad> outputQuads, TextureAtlasSprite sprite, PoseStack stack) {
            ModelUtility.toBakedQuads(part, outputQuads, sprite, stack);
    }

    private QuadCollection toUnculledCollection(List<BakedQuad> quads) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        return builder.build();
    }

    private void constructSingleVariants(List<BlockStateModelPart> parts) {
        for (BlockStateModelPart variant : parts) {
            models.add(new SingleVariant(variant));
        }
    }

    private void createModelPairs(String key) {
        pairs.put(key, models.getLast());
    }

    public Map<String, BlockStateModel> getPairs() {
        return pairs;
    }

    @Override
    public void collectParts(@NonNull RandomSource randomSource, @NonNull List<BlockStateModelPart> list) {
        if (models.isEmpty()) return;

        long seed = randomSource.nextLong();

        for (BlockStateModel model : this.models) {
            randomSource.setSeed(seed);
            model.collectParts(randomSource, list);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return null;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return 0;
    }
}
