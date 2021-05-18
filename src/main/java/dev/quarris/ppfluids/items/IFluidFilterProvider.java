package dev.quarris.ppfluids.items;

import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraft.item.ItemStack;

public interface IFluidFilterProvider {

    FluidFilter getFluidFilter(ItemStack module, FluidPipeTileEntity tile);

}
