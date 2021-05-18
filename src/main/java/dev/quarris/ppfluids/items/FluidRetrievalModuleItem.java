package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.misc.ItemEquality;
import de.ellpeck.prettypipes.misc.ItemFilter;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import java.util.Iterator;

import de.ellpeck.prettypipes.pipe.modules.retrieval.RetrievalModuleContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class FluidRetrievalModuleItem extends ModuleItem implements IFluidFilterProvider {
    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    public final int filterSlots;

    public FluidRetrievalModuleItem(String name, ModuleTier tier) {
        super(name);
        this.maxExtraction = tier.forTier(1, 8, 16);
        this.speed = tier.forTier(40, 20, 10);
        this.filterSlots = tier.forTier(3, 6, 9);
        this.preventOversending = tier.forTier(false, true, true);
    }

    public void tick(ItemStack module, PipeTileEntity tile) {
        if (tile instanceof FluidPipeTileEntity && tile.shouldWorkNow(this.speed) && tile.canWork()) {
            FluidPipeTileEntity pipe = (FluidPipeTileEntity) tile;
            PipeNetwork network = PipeNetwork.get(pipe.getWorld());

            for (FluidFilter filter : pipe.getFluidFilters()) {
                for(int slot = 0; slot < filter.getSlots(); ++slot) {
                    FluidStack filtered = filter.getFilter(slot);
                    if (!filtered.isEmpty()) {
                        FluidStack copy = filtered.copy();
                        copy.setAmount(this.maxExtraction);
                        Pair<BlockPos, ItemStack> dest = pipe.getAvailableDestination(copy, true, this.preventOversending);
                        if (dest != null) {
                            ItemStack toRequest = dest.getRight().copy();
                            //remain.shrink(network.getCurrentlyCraftingAmount(pipe.getPos(), copy, equalityTypes));
                            // Try and request fluid instead of item
                            if (PipeNetworkUtil.requestFluid(pipe.getWorld(), pipe.getPos(), dest.getLeft(), FluidItem.getFluidCopyFromItem(toRequest)).isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }

        }
    }

    public boolean canNetworkSee(ItemStack module, PipeTileEntity tile) {
        return false;
    }

    public boolean canAcceptItem(ItemStack module, PipeTileEntity tile, ItemStack stack) {
        return false;
    }

    public boolean isCompatible(ItemStack module, PipeTileEntity tile, IModule other) {
        return !(other instanceof FluidRetrievalModuleItem);
    }

    public boolean hasContainer(ItemStack module, PipeTileEntity tile) {
        return tile instanceof FluidPipeTileEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeTileEntity tile, int windowId, PlayerInventory inv, PlayerEntity player, int moduleIndex) {
        return new FluidRetrievalModuleContainer(ModContent.FLUID_RETRIEVAL_CONTAINER, windowId, player, tile.getPos(), moduleIndex);
    }

    @Override
    public FluidFilter getFluidFilter(ItemStack module, FluidPipeTileEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile);
        filter.canModifyWhitelist = false;
        filter.isWhitelist = true;
        return filter;
    }
}
