package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class FluidFilterModuleItem extends ModuleItem {

    public final int filterSlots;
    private final boolean canPopulateFromTanks;

    public FluidFilterModuleItem(String name, ModuleTier tier) {
        super(name);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.canPopulateFromTanks = tier.forTier(false, false, true);
    }

    public boolean canAcceptItem(ItemStack module, PipeTileEntity tile, ItemStack stack) {
        FluidFilter filter = this.getFluidFilter(module, (FluidPipeTileEntity)tile);
        return filter.isAllowed(stack);
    }

    public boolean isCompatible(ItemStack module, PipeTileEntity tile, IModule other) {
        return !(other instanceof FluidFilterModuleItem);
    }

    public boolean hasContainer(ItemStack module, PipeTileEntity tile) {
        return tile instanceof FluidPipeTileEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeTileEntity tile, int windowId, PlayerInventory inv, PlayerEntity player, int moduleIndex) {
        return new FluidFilterModuleContainer(ModContent.FLUID_FILTER_CONTAINER, windowId, player, tile.getPos(), moduleIndex);
    }

    public FluidFilter getFluidFilter(ItemStack module, FluidPipeTileEntity tile) {
        FluidFilter filter = new FluidFilter(this.filterSlots, module, tile);
        filter.canPopulateFromTanks = this.canPopulateFromTanks;
        return filter;
    }
}
