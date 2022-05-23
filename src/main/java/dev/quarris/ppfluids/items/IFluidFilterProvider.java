package dev.quarris.ppfluids.items;

import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.world.item.ItemStack;

public interface IFluidFilterProvider {

    FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile);

}
