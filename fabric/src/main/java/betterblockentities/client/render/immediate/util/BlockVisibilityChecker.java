package betterblockentities.client.render.immediate.util;

/* minecraft */
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* java/misc */
import java.util.*;

/**
 * Utility class for checking if a block entity is visible (frustum)
 */
public final class BlockVisibilityChecker {
    private static final double MAX_DIST_SQ = 20.0 * 20.0;

    private BlockVisibilityChecker() { }

    public static Visibility isBlockInFOVAndVisible(Frustum frustum, BlockEntity blockEntity) {
        /* check if we have a screen open (we count this as "not" visible) */
        Screen curScreen = Minecraft.getInstance().screen;
        if (curScreen != null && !(curScreen instanceof ChatScreen)) return Visibility.OCCLUDED;

        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player == null) return Visibility.OCCLUDED;

        Vec3 eye = player.getEyePosition(1.0f);

        AABB box = setupBox(blockEntity, blockEntity.getBlockPos());

        double cx = (box.minX + box.maxX) * 0.5;
        double cy = (box.minY + box.maxY) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;

        double dx = eye.x - cx, dy = eye.y - cy, dz = eye.z - cz;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq > MAX_DIST_SQ) return Visibility.OCCLUDED;

        if (frustum == null || !frustum.isVisible(box)) return Visibility.OCCLUDED;

        return Visibility.VISIBLE;
    }

    private static AABB setupBox(BlockEntity blockEntity, BlockPos pos) {
        if (blockEntity instanceof BannerBlockEntity) return new AABB(pos).inflate(0, 1, 0);
        if (!(blockEntity instanceof ChestBlockEntity)) return new AABB(pos);

        BlockPos other = getOppositeChestHalf(blockEntity.getLevel(), pos);
        return (other == null) ? new AABB(pos) : new AABB(pos).minmax(new AABB(other));
    }

    public static BlockPos getOppositeChestHalf(Level world, BlockPos pos) {
        if (world == null) return null;

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock)) return null;

        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type == ChestType.SINGLE) return null;

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction side = (type == ChestType.LEFT) ? facing.getClockWise() : facing.getCounterClockWise();

        BlockPos otherPos = pos.relative(side);

        if (world.getBlockState(otherPos).getBlock() instanceof ChestBlock) return otherPos;
        return null;
    }

    public static ChestBlockEntity getOtherChestHalf(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ChestBlock)) return null;

        ChestType type = state.getValue(ChestBlock.TYPE);
        Direction facing = state.getValue(ChestBlock.FACING);

        Direction side;
        if (type == ChestType.LEFT) {
            side = facing.getClockWise();
        } else if (type == ChestType.RIGHT) {
            side = facing.getCounterClockWise();
        } else {
            return null;
        }

        BlockPos otherPos = pos.offset(side.getUnitVec3i());
        BlockEntity be = level.getBlockEntity(otherPos);

        return be instanceof ChestBlockEntity ? (ChestBlockEntity) be : null;
    }

    public enum Visibility {
        VISIBLE,
        OCCLUDED,
        UNKNOWN
    }
}
