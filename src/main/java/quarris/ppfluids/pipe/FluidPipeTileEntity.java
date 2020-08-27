package quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.misc.ItemEqualityType;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import quarris.ppfluids.ModContent;
import quarris.ppfluids.items.FluidItem;
import quarris.ppfluids.network.FluidPipeItem;

public class FluidPipeTileEntity extends PipeTileEntity {

    public FluidPipeTileEntity() {
        super(ModContent.FLUID_PIPE_TILE);
    }

    @Override
    public Pair<BlockPos, ItemStack> getAvailableDestination(ItemStack stack, boolean force, boolean preventOversending) {
        if (!this.canWork()) {
            return null;
        } else if (!force && this.streamModules().anyMatch(m -> !(m.getRight()).canAcceptItem(m.getLeft(), this, stack))) {
            return null;
        } else {
            Direction[] directions = Direction.values();
            int size = directions.length;

            for(int i = 0; i < size; ++i) {
                Direction dir = directions[i];
                IFluidHandler handler = this.getFluidHandler(dir, null);
                if (handler != null) {
                    FluidStack fluid = FluidItem.createFluidFromItem(stack);
                    int filled = handler.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                    if (filled > 0) {
                        FluidStack toInsert = fluid.copy();
                        toInsert.setAmount(filled);
                        int maxAmount = filled;
                        //this.streamModules().mapToInt((m) -> ((IModule)m.getRight()).getMaxInsertionAmount(m.getLeft(), this, stack, handler)).min().orElse(2147483647);
                        if (maxAmount < toInsert.getAmount()) {
                            toInsert.setAmount(maxAmount);
                        }

                        BlockPos offset = this.pos.offset(dir);
                        // TODO Look at this and make it work with fluids
                        if (preventOversending || maxAmount < Integer.MAX_VALUE) {
                            PipeNetwork network = PipeNetwork.get(this.world);
                            int onTheWay = network.getItemsOnTheWay(offset, null);
                            if (onTheWay > 0) {
                                if (maxAmount < 2147483647) {
                                    int onTheWaySame = network.getItemsOnTheWay(offset, stack);
                                    if (toInsert.getAmount() + onTheWaySame > maxAmount) {
                                        toInsert.setAmount(maxAmount - onTheWaySame);
                                    }
                                }

                                FluidStack copy = fluid.copy();
                                copy.setAmount(Integer.MAX_VALUE);
                                int totalSpace = handler.fill(copy, IFluidHandler.FluidAction.SIMULATE);

                                if (onTheWay + toInsert.getAmount() > totalSpace) {
                                    toInsert.setAmount(totalSpace - onTheWay);
                                }
                            }
                        }

                        if (!toInsert.isEmpty()) {
                            return Pair.of(offset, FluidItem.createItemFromFluid(toInsert));
                        }
                    }
                }
            }

            return null;
        }
    }

    @Override
    public boolean isConnectedInventory(Direction dir) {
        return this.getFluidHandler(dir, null) != null;
    }

    public IFluidHandler getFluidHandler(Direction dir, FluidPipeItem item) {
        if (!this.isConnected(dir)) {
            return null;
        } else {
            BlockPos pos = this.pos.offset(dir);
            TileEntity tile = this.world.getTileEntity(pos);
            if (tile != null) {
                IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()).orElse(null);
                if (handler != null) {
                    return handler;
                }
            }

            return null;
            //IPipeConnectable connectable = this.getPipeConnectable(dir);
            //return connectable != null ? connectable.getItemHandler(this.world, this.pos, dir, item) : null;
        }
    }
}
