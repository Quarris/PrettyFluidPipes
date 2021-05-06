package quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.pipe.ConnectionType;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class FluidPipeBlock extends PipeBlock {

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new FluidPipeTileEntity();
    }

    @Override
    protected ConnectionType getConnectionType(World world, BlockPos pos, Direction direction, BlockState state) {
        BlockPos offset = pos.offset(direction);
        if (!world.isBlockLoaded(offset)) {
            return ConnectionType.DISCONNECTED;
        }

        Direction opposite = direction.getOpposite();

        TileEntity tile = world.getTileEntity(offset);
        if (tile != null) {
            IPipeConnectable connectable = tile.getCapability(Registry.pipeConnectableCapability, opposite).orElse(null);
            if (connectable != null)
                return connectable.getConnectionType(pos, direction);

            if (tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite).isPresent()) {
                return ConnectionType.CONNECTED;
            }
        }

        BlockState offState = world.getBlockState(offset);
        if (hasLegsTo(world, offState, offset, direction)) {
            if (DIRECTIONS.values().stream().noneMatch((d) -> state.get(d) == ConnectionType.LEGS)) {
                return ConnectionType.LEGS;
            }
        }

        return ConnectionType.DISCONNECTED;
    }
}
