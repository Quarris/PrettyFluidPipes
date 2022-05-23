package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        this.preventOversending = tier.forTier(true, true, true);
    }

    public void tick(ItemStack module, PipeBlockEntity tile) {
        if (tile instanceof FluidPipeBlockEntity && tile.shouldWorkNow(this.speed) && tile.canWork()) {
            FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) tile;
            PipeNetwork network = PipeNetwork.get(pipe.getLevel());

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
                            if (PipeNetworkUtil.requestFluid(pipe.getLevel(), pipe.getBlockPos(), dest.getLeft(), FluidItem.getFluidCopyFromItem(toRequest)).isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }

        }
    }

    public boolean canNetworkSee(ItemStack module, PipeBlockEntity tile) {
        return false;
    }

    public boolean canAcceptItem(ItemStack module, PipeBlockEntity tile, ItemStack stack) {
        return false;
    }

    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return tile instanceof FluidPipeBlockEntity && !(other instanceof FluidRetrievalModuleItem);
    }

    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return tile instanceof FluidPipeBlockEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidRetrievalModuleContainer(ModContent.FLUID_RETRIEVAL_CONTAINER.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    @Override
    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile);
        filter.canModifyWhitelist = false;
        filter.isWhitelist = true;
        return filter;
    }
}
