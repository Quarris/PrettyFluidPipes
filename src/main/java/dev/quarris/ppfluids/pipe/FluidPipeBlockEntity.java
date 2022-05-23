package dev.quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.items.IFluidFilterProvider;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeBlockEntity extends PipeBlockEntity {

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = ModContent.FLUID_PIPE_TILE.get();
    }

    public Pair<BlockPos, ItemStack> getAvailableDestination(FluidStack fluid, boolean force, boolean preventOversending) {
        if (!this.canWork()) {
            return null;
        }
        if (!force && this.streamModules().anyMatch(m -> !(m.getRight()).canAcceptItem(m.getLeft(), this, FluidItem.createItemFromFluid(fluid, false)))) {
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

                    BlockPos tankPos = this.getBlockPos().relative(dir);
                    if (preventOversending || maxAmount < Integer.MAX_VALUE) {
                        PipeNetwork network = PipeNetwork.get(this.level);
                        int onTheWay = network.getPipeItemsOnTheWay(tankPos)
                                .filter(item -> item instanceof FluidPipeItem)
                                .map(item -> (FluidPipeItem) item)
                                .filter(item -> toInsert.getFluid().isSame(item.getFluidContent().getFluid()))
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
                        return Pair.of(tankPos, FluidItem.createItemFromFluid(toInsert, false));
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
    public boolean isConnectedInventory(Direction dir) {
        return this.getAdjacentFluidHandler(dir) != null;
    }

    public IFluidHandler getAdjacentFluidHandler(Direction dir) {
        if (!this.isConnected(dir)) {
            return null;
        }

        BlockPos pos = this.getBlockPos().relative(dir);
        BlockEntity tile = this.level.getBlockEntity(pos);
        if (tile != null) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite()).orElse(null);
            if (handler != null) {
                return handler;
            }
        }

        return null;
    }

    @Override
    public IItemHandler getItemHandler(Direction dir) {
        IFluidHandler fluidHandler = this.getAdjacentFluidHandler(dir);
        if (fluidHandler != null) {

        }
        return null;
    }

    public List<FluidFilter> getFluidFilters() {
        return this.streamModules()
                .filter(p -> p.getRight() instanceof IFluidFilterProvider)
                .map(p -> ((IFluidFilterProvider) p.getRight()).getFluidFilter(p.getLeft(), this))
                .collect(Collectors.toList());
    }
}
