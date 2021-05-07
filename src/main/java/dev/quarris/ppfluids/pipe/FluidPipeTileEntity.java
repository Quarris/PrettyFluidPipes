package dev.quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.network.FluidPipeItem;

import java.util.List;

public class FluidPipeTileEntity extends PipeTileEntity {

    public FluidPipeTileEntity() {
        super(ModContent.FLUID_PIPE_TILE);
    }

    public Pair<BlockPos, ItemStack> getAvailableDestination(FluidStack fluid, boolean force, boolean preventOversending) {

        if (!this.canWork()) {
            return null;
        }
        if (!force && this.streamModules().anyMatch(m -> !(m.getRight()).canAcceptItem(m.getLeft(), this, FluidItem.createItemFromFluid(fluid)))) {
            return null;
        }

        for (Direction dir : Direction.values()) {
            IFluidHandler tank = this.getAdjacentFluidHandler(dir);
            if (tank != null) {
                int amountFilled = tank.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                if (amountFilled > 0) {
                    FluidStack toInsert = fluid.copy();
                    toInsert.setAmount(amountFilled);
                    // TODO Replace maxAmount once a limiting module is implemented
                    int maxAmount = Integer.MAX_VALUE;
                    //int maxAmount = this.streamModules().mapToInt((m) -> ((IModule)m.getRight()).getMaxInsertionAmount(m.getLeft(), this, stack, handler)).min().orElse(Integer.MAX_VALUE);
                    if (maxAmount < toInsert.getAmount()) {
                        toInsert.setAmount(maxAmount);
                    }

                    BlockPos tankPos = this.pos.offset(dir);
                    if (preventOversending || maxAmount < Integer.MAX_VALUE) {
                        PipeNetwork network = PipeNetwork.get(this.world);
                        int onTheWay = network.getPipeItemsOnTheWay(tankPos)
                                .filter(item -> item instanceof FluidPipeItem)
                                .map(item -> (FluidPipeItem) item)
                                .filter(item -> toInsert.getFluid().isEquivalentTo(item.getFluidContent().getFluid()))
                                .mapToInt(item -> item.getFluidContent().getAmount())
                                .sum();

                        if (onTheWay > 0) {
                            FluidStack copy = toInsert.copy();
                            copy.setAmount(Integer.MAX_VALUE);
                            int totalSpace = tank.fill(copy, IFluidHandler.FluidAction.SIMULATE);
                            if (onTheWay + toInsert.getAmount() > totalSpace) {
                                toInsert.setAmount(totalSpace - onTheWay);
                            }
                        }
                    }

                    if (!toInsert.isEmpty()) {
                        return Pair.of(tankPos, FluidItem.createItemFromFluid(toInsert));
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Pair<BlockPos, ItemStack> getAvailableDestination(ItemStack stack, boolean force, boolean preventOversending) {
        return null;
    }

    @Override
    public List<IPipeItem> getItems() {
        if (this.items == null) {
            this.items = PipeNetwork.get(this.world).getItemsInPipe(this.pos);
            /*
            for (int i = 0, itemsSize = this.items.size(); i < itemsSize; i++) {
                IPipeItem item = this.items.get(i);
                if (item.getContent().getItem() == ModContent.FLUID_ITEM) {
                    FluidPipeItem fluidItem = new FluidPipeItem(item.serializeNBT());
                    this.items.set(i, fluidItem);
                }
            }
             */
        }
        return this.items;
    }

    @Override
    public boolean isConnectedInventory(Direction dir) {
        return this.getAdjacentFluidHandler(dir) != null;
    }

    public IFluidHandler getAdjacentFluidHandler(Direction dir) {
        if (!this.isConnected(dir)) {
            return null;
        }

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
