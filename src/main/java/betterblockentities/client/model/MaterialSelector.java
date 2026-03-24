package betterblockentities.client.model;

/* minecraft */
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.entity.*;

/* java/misc */
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * rebuild these so we can safely use them asynchronously when meshing
 * to avoid invoking these from each renderer or risking concurrency (not thread-safe)
 */
public class MaterialSelector {
    private static final ConcurrentHashMap<Identifier, SpriteId> BANNER_MATERIALS = new ConcurrentHashMap<>();

    public static SpriteId getBannerMaterial(Holder<BannerPattern> holder) {
        Identifier id = holder.value().assetId();
        SpriteMapper mapper = Sheets.BANNER_MAPPER;
        return BANNER_MATERIALS.computeIfAbsent(id, mapper::apply);
    }

    public static SpriteId getDPSideMaterial(Optional<Item> optional) {
        if (optional.isPresent()) {
            SpriteId material = Sheets.getDecoratedPotSprite(DecoratedPotPatterns.getPatternFromItem((Item)optional.get()));
            if (material != null) {
                return material;
            }
        }
        return Sheets.DECORATED_POT_SIDE;
    }

    public static ChestRenderState.ChestMaterialType getChestMaterial(BlockEntity blockEntity, boolean bl) {
        if (blockEntity instanceof EnderChestBlockEntity) {
            return ChestRenderState.ChestMaterialType.ENDER_CHEST;
        } else if (bl) {
            return ChestRenderState.ChestMaterialType.CHRISTMAS;
        } else if (blockEntity instanceof TrappedChestBlockEntity) {
            return ChestRenderState.ChestMaterialType.TRAPPED;
        } else if (blockEntity.getBlockState().getBlock() instanceof CopperChestBlock copperChestBlock) {
            return switch (copperChestBlock.getState()) {
                case UNAFFECTED -> ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED;
                case EXPOSED -> ChestRenderState.ChestMaterialType.COPPER_EXPOSED;
                case WEATHERED -> ChestRenderState.ChestMaterialType.COPPER_WEATHERED;
                case OXIDIZED -> ChestRenderState.ChestMaterialType.COPPER_OXIDIZED;
            };
        } else {
            return ChestRenderState.ChestMaterialType.REGULAR;
        }
    }
}
