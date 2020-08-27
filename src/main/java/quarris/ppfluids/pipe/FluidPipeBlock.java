package quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.pipe.ConnectionType;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.block.Block;
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
        } else {
            BlockState offState = world.getBlockState(offset);
            Block block = offState.getBlock();
            if (block instanceof IPipeConnectable) {
                return ((IPipeConnectable)block).getConnectionType(world, pos, direction);
            } else {
                TileEntity tile = world.getTileEntity(offset);
                if (tile != null) {
                    if (tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
                        return ConnectionType.CONNECTED;
                    }
                }

                return hasLegsTo(world, offState, offset, direction) && DIRECTIONS.values().stream().noneMatch((d) -> state.get(d) == ConnectionType.LEGS) ? ConnectionType.LEGS : ConnectionType.DISCONNECTED;
            }
        }
    }
}
