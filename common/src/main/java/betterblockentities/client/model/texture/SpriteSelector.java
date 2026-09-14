package betterblockentities.client.model.texture;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/* java/misc */
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rebuild these so we can safely use them asynchronously when meshing
 * to avoid invoking these from each renderer or risking concurrency (not thread-safe)
 */
public class SpriteSelector {
    private static final ConcurrentHashMap<Identifier, SpriteId> BANNER_MATERIALS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ItemInstance, SpriteId> DECORATED_POT_MATERIALS = new ConcurrentHashMap<>();

    public static TextureAtlasSprite getBannerPatternSprite(Holder<BannerPattern> holder) {
        Identifier id = holder.value().assetId();
        SpriteMapper mapper = Sheets.BANNER_MAPPER;
        SpriteId material = BANNER_MATERIALS.computeIfAbsent(id, mapper::apply);
        return getBlockSprite(material.texture());
    }

    public static TextureAtlasSprite getDecoratedPotSideSprite(Optional<? extends ItemInstance> item) {
        if (item.isPresent()) {
            SpriteId material = DECORATED_POT_MATERIALS.computeIfAbsent(
                    item.get(), key -> getDecoratedPotMaterial(Optional.of(key)));
            if (material != null) {
                return getBlockSprite(material.texture());
            }
        }
        return getBlockSprite(Sheets.DECORATED_POT_SIDE.texture());
    }

    public static SpriteId getDecoratedPotMaterial(Optional<? extends ItemInstance> item) {
        if (item.isPresent()) {
            Holder<DecoratedPotPattern> pattern = item.get().get(DataComponents.PROVIDES_POTTERY_PATTERN);
            if (pattern != null) {
                return Sheets.DECORATED_POT_MAPPER.apply(pattern.value().assetId());
            }
            return null;
        } else {
            return Sheets.DECORATED_POT_SIDE;
        }
    }

    public static TextureAtlasSprite getCopperGolemStatueSprite(CopperGolemStatueBlock cgsBlock) {
        final Identifier texture = CopperGolemOxidationLevels.getOxidationLevel(cgsBlock.getWeatheringState()).texture();

        String path = texture.getPath();
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }

        Identifier strippedTexture = Identifier.withDefaultNamespace(path);
        return SpriteSelector.getBlockSprite(strippedTexture);
    }

    public static TextureAtlasSprite getChestSprite(BlockState state, BlockEntity blockEntity, boolean bl) {
        ChestRenderState.ChestMaterialType materialType = ChestRenderState.ChestMaterialType.REGULAR;

        if (blockEntity.getBlockState().getBlock() instanceof CopperChestBlock copperChestBlock) {
            switch (copperChestBlock.getState()) {
                case UNAFFECTED -> materialType = ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED;
                case EXPOSED -> materialType = ChestRenderState.ChestMaterialType.COPPER_EXPOSED;
                case WEATHERED -> materialType = ChestRenderState.ChestMaterialType.COPPER_WEATHERED;
                case OXIDIZED -> materialType = ChestRenderState.ChestMaterialType.COPPER_OXIDIZED;
            };
        } else if (blockEntity instanceof EnderChestBlockEntity) {
            materialType = ChestRenderState.ChestMaterialType.ENDER_CHEST;
        } else if (bl) {
            materialType = ChestRenderState.ChestMaterialType.CHRISTMAS;
        } else if (blockEntity instanceof TrappedChestBlockEntity) {
            materialType = ChestRenderState.ChestMaterialType.TRAPPED;
        }

        final ChestType type = state.hasProperty(ChestBlock.TYPE) ?
                state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;

        return getBlockSprite(Sheets.chooseSprite(materialType, type).texture());
    }

    public static TextureAtlasSprite getShulkerBoxSprite(ShulkerBoxBlock block) {
        DyeColor color = block.getColor();
        SpriteId shulkerMaterial = color == null ?
                Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION : Sheets.getShulkerBoxSprite(color);

        return getBlockSprite(shulkerMaterial.texture());
    }

    public static TextureAtlasSprite getBlockSprite(Identifier id) {
        var atlas = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS);
        return atlas.getSprite(id);
    }

    public record PotSideSprite(RenderType renderType, TextureAtlasSprite sprite) {
        public static PotSideSprite create(final SpriteGetter sprites, final SpriteId spriteId) {
            return new PotSideSprite(spriteId.renderType(RenderTypes::entitySolid), sprites.get(spriteId));
        }
    }
}
