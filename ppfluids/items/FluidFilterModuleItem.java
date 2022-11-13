package old.dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import old.dev.quarris.ppfluids.ModContent;
import old.dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import old.dev.quarris.ppfluids.misc.FluidDirectionSelector;
import old.dev.quarris.ppfluids.misc.FluidFilter;
import old.dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidFilterModuleItem extends FluidModuleItem implements IFluidFilterProvider {

    public final int filterSlots;
    private final boolean canPopulateFromTanks;

    public FluidFilterModuleItem(String name, ModuleTier tier) {
        super(name);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.canPopulateFromTanks = tier.forTier(false, false, true);
    }

    public boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IFluidHandler destination) {
        if (!(pipe instanceof FluidPipeBlockEntity)) return false;
        FluidFilter filter = this.getFluidFilter(module, (FluidPipeBlockEntity) pipe);
        return filter.isPipeItemAllowed(stack);
    }

    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return tile instanceof FluidPipeBlockEntity && !(other instanceof FluidFilterModuleItem);
    }

    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return tile instanceof FluidPipeBlockEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidFilterModuleContainer(ModContent.FLUID_FILTER_CONTAINER.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    @Override
    public DirectionSelector getDirectionSelector(ItemStack module, PipeBlockEntity tile) {
        return new FluidDirectionSelector(module, tile);
    }

    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile);
        filter.canPopulateFromTanks = this.canPopulateFromTanks;
        return filter;
    }
}
