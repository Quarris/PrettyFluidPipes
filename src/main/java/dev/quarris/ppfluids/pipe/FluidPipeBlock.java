package dev.quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.pipe.ConnectionType;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.registry.BlockEntitySetup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends PipeBlock {

    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitySetup.FLUID_PIPE.get(), PipeBlockEntity::tick);
    }

    @Override
    protected ConnectionType getConnectionType(Level level, BlockPos pos, Direction direction, BlockState state) {
        BlockPos offset = pos.relative(direction);
        if (!level.isLoaded(offset)) {
            return ConnectionType.DISCONNECTED;
        }

        Direction opposite = direction.getOpposite();

        BlockEntity tile = level.getBlockEntity(offset);
        if (tile != null) {
            IPipeConnectable connectable = level.getCapability(Registry.pipeConnectableCapability, offset, opposite);
            if (connectable != null) {
                return connectable.getConnectionType(pos, direction);
            }

            if (level.getCapability(Capabilities.FluidHandler.BLOCK, offset, opposite) != null) {
                return ConnectionType.CONNECTED;
            }
        }

        BlockState offState = level.getBlockState(offset);
        if (hasLegsTo(level, offState, offset, direction)) {
            if (DIRECTIONS.values().stream().noneMatch((d) -> state.getValue(d) == ConnectionType.LEGS)) {
                return ConnectionType.LEGS;
            }
        }

        return ConnectionType.DISCONNECTED;
    }
}
