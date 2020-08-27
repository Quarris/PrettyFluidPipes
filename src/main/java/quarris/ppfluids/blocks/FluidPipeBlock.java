package quarris.ppfluids.blocks;

import de.ellpeck.prettypipes.pipe.ConnectionType;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class FluidPipeBlock extends PipeBlock {

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockState newState = this.createState(worldIn, pos, state);
        if (newState != state) {
            worldIn.setBlockState(pos, newState);
            onStateChanged(worldIn, pos, newState);
        }

    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.createState(context.getWorld(), context.getPos(), this.getDefaultState());
    }

    private BlockState createState(World world, BlockPos pos, BlockState curr) {
        BlockState state = this.getDefaultState();
        IFluidState fluid = world.getFluidState(pos);
        if (fluid.isTagged(FluidTags.WATER) && fluid.getLevel() == 8) {
            state = state.with(BlockStateProperties.WATERLOGGED, true);
        }

        Direction[] directions = Direction.values();
        int size = directions.length;

        for(int i = 0; i < size; ++i) {
            Direction dir = directions[i];
            EnumProperty<ConnectionType> prop = DIRECTIONS.get(dir);
            ConnectionType type = getConnectionType(world, pos, dir, state);
            if (type.isConnected() && curr.get(prop) == ConnectionType.BLOCKED) {
                type = ConnectionType.BLOCKED;
            }

            state = state.with(prop, type);
        }

        return state;
    }

    private static ConnectionType getConnectionType(World world, BlockPos pos, Direction direction, BlockState state) {
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

    private static boolean hasLegsTo(World world, BlockState state, BlockPos pos, Direction direction) {
        if (!(state.getBlock() instanceof WallBlock) && !(state.getBlock() instanceof FenceBlock)) {
            return state.getMaterial() != Material.ROCK && state.getMaterial() != Material.IRON ? false : hasSolidSide(state, world, pos, direction.getOpposite());
        } else {
            return direction == Direction.DOWN;
        }
    }

}
