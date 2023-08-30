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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends PipeBlock {

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
            IPipeConnectable connectable = tile.getCapability(Registry.pipeConnectableCapability, opposite).orElse(null);
            if (connectable != null)
                return connectable.getConnectionType(pos, direction);

            if (tile.getCapability(ForgeCapabilities.FLUID_HANDLER, opposite).isPresent()) {
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
