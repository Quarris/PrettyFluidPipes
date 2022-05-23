package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FluidFilterModuleItem extends ModuleItem implements IFluidFilterProvider {

    public final int filterSlots;
    private final boolean canPopulateFromTanks;

    public FluidFilterModuleItem(String name, ModuleTier tier) {
        super(name);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.canPopulateFromTanks = tier.forTier(false, false, true);
    }

    public boolean canAcceptItem(ItemStack module, PipeBlockEntity tile, ItemStack stack) {
        if (!(tile instanceof FluidPipeBlockEntity)) return super.canAcceptItem(module, tile, stack);
        FluidFilter filter = this.getFluidFilter(module, (FluidPipeBlockEntity)tile);
        return filter.isAllowed(stack);
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

    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile);
        filter.canPopulateFromTanks = this.canPopulateFromTanks;
        return filter;
    }
}
