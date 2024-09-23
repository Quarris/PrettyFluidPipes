package dev.quarris.ppfluids.item;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.misc.FluidDirectionSelector;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class FluidRetrievalModuleItem extends FluidModuleItem implements IFluidFilterProvider {

    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    public final int filterSlots;

    public FluidRetrievalModuleItem(String name, ModuleTier tier) {
        super(name);
        this.maxExtraction = tier.forTier(500, 2000, 8000);
        this.speed = tier.forTier(40, 20, 10);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.preventOversending = true;
    }

    @Override
    public void tick(ItemStack module, PipeBlockEntity tile) {
        if (!(tile instanceof FluidPipeBlockEntity fluidPipe) || !tile.shouldWorkNow(this.speed) || !tile.canWork())
            return;

        Direction[] directions = this.getDirectionSelector(module, tile).directions();

        for (FluidFilter subFilter : fluidPipe.getFluidFilters()) {
            for (int slot = 0; slot < subFilter.getSlots(); slot++) {
                ItemStack filteredBucket = subFilter.getStackInSlot(slot);
                if (filteredBucket.isEmpty())
                    continue;

                Optional<FluidStack> filtered = FluidUtil.getFluidContained(filteredBucket);

                if (!filtered.isPresent())
                    continue;

                FluidStack copy = filtered.get().copy();
                copy.setAmount(this.maxExtraction);
                Pair<BlockPos, ItemStack> dest = fluidPipe.getAvailableDestination(directions, copy, true, this.preventOversending);
                if (dest == null)
                    continue;
                ItemStack fluidItem = dest.getRight().copy();
                //remain.shrink(network.getCurrentlyCraftingAmount(pipe.getPos(), copy, equalityTypes));
                // Try and request fluid instead of item
                if (PipeNetworkUtil.requestFluid(fluidPipe.getLevel(), fluidPipe.getBlockPos(), dest.getLeft(), FluidItem.getFluidCopyFromItem(fluidItem)).isEmpty()) {
                    break;
                }
            }
        }
    }

    private FluidStack requestFilteredFromNetwork(Level level, BlockPos pos, FluidFilter filter) {
        for (var location : PipeNetworkUtil.getOrderedNetworkFluids(level, pos)) {
            var fluid = location.getFirstAvailableFluid(level);
            if (filter.isPipeFluidAllowed(fluid)) {
                return fluid;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canNetworkSee(ItemStack module, PipeBlockEntity tile, Direction direction, IItemHandler handler) {
        return tile instanceof FluidPipeBlockEntity && !this.getDirectionSelector(module, tile).has(direction);
    }

    @Override
    public boolean canAcceptItem(ItemStack module, PipeBlockEntity tile, ItemStack stack, Direction direction, IItemHandler destination) {
        return tile instanceof FluidPipeBlockEntity && !this.getDirectionSelector(module, tile).has(direction);
    }

    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return tile instanceof FluidPipeBlockEntity && !(other instanceof FluidRetrievalModuleItem);
    }

    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return tile instanceof FluidPipeBlockEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidRetrievalModuleContainer(MenuSetup.FLUID_RETRIEVAL_CONTAINER.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    @Override
    public DirectionSelector getDirectionSelector(ItemStack module, PipeBlockEntity tile) {
        return new FluidDirectionSelector(module, tile);
    }

    @Override
    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile, true);
        return filter;
    }
}
