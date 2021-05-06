package quarris.ppfluids.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.ItemFluidContainer;
import quarris.ppfluids.ModContent;
import quarris.ppfluids.client.FluidISTER;

public class FluidItem extends ItemFluidContainer {

    public FluidItem() {
        super(new Item.Properties().setISTER(() -> FluidISTER::new), Integer.MAX_VALUE);
    }

    public static ItemStack createItemFromFluid(FluidStack fluid) {
        ItemStack item =  new ItemStack(ModContent.FLUID_ITEM);
        IFluidHandlerItem tank = FluidUtil.getFluidHandler(item).orElse(null);
        if (tank != null) {
            tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        }
        return item;
    }

    public static FluidStack getFluidCopyFromItem(ItemStack item) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(item).orElse(null);
        if (handler != null) {
            return handler.getFluidInTank(0).copy();
        }
        return FluidStack.EMPTY;
    }

    public static ItemStack insertFluid(IFluidHandler handler, ItemStack fluidItem, boolean simulate) {
        FluidStack fluidStack = FluidItem.getFluidCopyFromItem(fluidItem);
        int filled = handler.fill(fluidStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        fluidStack.shrink(filled);

        if (fluidStack.isEmpty())
            return ItemStack.EMPTY;

        return FluidItem.createItemFromFluid(fluidStack);
    }
}
