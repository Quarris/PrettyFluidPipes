package old.dev.quarris.ppfluids.items;

import old.dev.quarris.ppfluids.misc.FluidFilter;
import old.dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.world.item.ItemStack;

public interface IFluidFilterProvider {

    FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile);

}
