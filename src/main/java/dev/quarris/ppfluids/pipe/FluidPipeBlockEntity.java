package dev.quarris.ppfluids.pipe;

import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeConnectable;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.item.FluidItem;
import dev.quarris.ppfluids.item.IFluidFilterProvider;
import dev.quarris.ppfluids.item.IFluidModule;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import dev.quarris.ppfluids.registry.BlockEntitySetup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeBlockEntity extends PipeBlockEntity {

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitySetup.FLUID_PIPE.get(), pos, state);
    }

    @Override
    public Pair<BlockPos, ItemStack> getAvailableDestinationOrConnectable(ItemStack stack, boolean force, boolean preventOversending) {
        return super.getAvailableDestinationOrConnectable(stack, force, preventOversending);
    }

    @Override
    public Pair<BlockPos, ItemStack> getAvailableDestination(Direction[] directions, ItemStack stack, boolean force, boolean preventOversending) {
        return super.getAvailableDestination(directions, stack, force, preventOversending);
    }

    public Pair<BlockPos, ItemStack> getAvailableDestination(Direction[] directions, FluidStack fluid, boolean force, boolean preventOversending) {
        if (!this.canWork()) {
            return null;
        }

        for (Direction dir : directions) {
            IFluidHandler tank = this.getFluidHandler(dir);
            if (tank == null)
                continue;

            if (!force && this.streamModules().filter(m -> m.getRight() instanceof IFluidModule)
                .anyMatch(m -> !((IFluidModule) m.getRight()).canAcceptItem(m.getLeft(), this, FluidItem.createItemFromFluid(fluid), dir, tank))) {
                continue;
            }
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
                        int availableSpace = tank.fill(copy, IFluidHandler.FluidAction.SIMULATE);
                        if (onTheWay + toInsert.getAmount() > availableSpace) {
                            toInsert.setAmount(availableSpace - onTheWay);
                        }
                    }
                }

                if (!toInsert.isEmpty()) {
                    return Pair.of(tankPos, FluidItem.createItemFromFluid(toInsert));
                }
            }
        }

        return null;
    }

    @Override
    public boolean canNetworkSee(Direction direction, IItemHandler handler) {
        return false;
    }

    public boolean canNetworkSee(Direction direction, IFluidHandler handler) {
        return this.streamModules().filter(m -> m.getRight() instanceof IFluidModule).allMatch((m) -> ((IFluidModule) m.getRight()).canNetworkSee(m.getLeft(), this, direction, handler));
    }

    @Override
    public boolean canHaveModules() {
        for(Direction dir : Direction.values()) {
            if (this.getFluidHandler(dir) != null) {
                return true;
            }

            IPipeConnectable connectable = this.getPipeConnectable(dir);
            if (connectable != null && connectable.allowsModules(this.worldPosition, dir)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isConnected(Direction dir) {
        return super.isConnected(dir); //this.getAdjacentFluidHandler(dir) != null;
    }

    public IFluidHandler getFluidHandler(Direction dir) {
        if (!this.isConnected(dir)) {
            return null;
        }

        BlockPos pos = this.getBlockPos().relative(dir);
        BlockEntity tile = this.level.getBlockEntity(pos);
        if (tile != null) {
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, dir.getOpposite());
            if (handler != null) {
                return handler;
            }
        }

        return null;
    }

    @Override
    public IItemHandler getItemHandler(Direction dir) {
        IFluidHandler fluidHandler = this.getFluidHandler(dir);
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
