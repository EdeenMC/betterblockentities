package betterblockentities.data;

/* local */
import net.minecraft.world.level.block.entity.BlockEntityType;

public enum SupportedBlockEntityTypes {
    CHEST(BlockEntityType.CHEST),
    ENDER_CHEST(BlockEntityType.ENDER_CHEST),
    TRAPPED_CHEST(BlockEntityType.TRAPPED_CHEST),
    SIGN(BlockEntityType.SIGN),
    HANGING_SIGN(BlockEntityType.HANGING_SIGN),
    SHULKER_BOX(BlockEntityType.SHULKER_BOX),
    DECORATED_POT(BlockEntityType.DECORATED_POT),
    BANNER(BlockEntityType.BANNER),
    BELL(BlockEntityType.BELL),
    BED(BlockEntityType.BED),
    COPPER_GOLEM_STATUE(BlockEntityType.COPPER_GOLEM_STATUE);

    private final BlockEntityType<?> blockEntityType;

    SupportedBlockEntityTypes(BlockEntityType<?> blockEntityType) {
        this.blockEntityType = blockEntityType;
    }

    public BlockEntityType<?> type() {
        return this.blockEntityType;
    }
}
