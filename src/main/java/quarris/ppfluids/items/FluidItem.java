package quarris.ppfluids.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import quarris.ppfluids.ModContent;

public class FluidItem extends Item {

    public FluidItem() {
        super(new Item.Properties());
    }

    public static ItemStack createItemFromFluid(FluidStack fluid) {
        ItemStack item =  new ItemStack(ModContent.FLUID_ITEM);
        item.getOrCreateTag().put("Fluid", fluid.writeToNBT(new CompoundNBT()));
        return item;
    }

    public static FluidStack createFluidFromItem(ItemStack item) {
        if (item.isEmpty())
            return FluidStack.EMPTY;

        CompoundNBT nbt = item.getChildTag("Fluid");
        if (nbt != null) {
            return FluidStack.loadFluidStackFromNBT(nbt);
        }

        return FluidStack.EMPTY;
    }

    public static ItemStack insertFluid(IFluidHandler handler, ItemStack fluidItem, boolean simulate) {
        FluidStack fluidStack = FluidItem.createFluidFromItem(fluidItem);
        int filled = handler.fill(fluidStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        fluidStack.shrink(filled);

        if (fluidStack.isEmpty())
            return ItemStack.EMPTY;

        return FluidItem.createItemFromFluid(fluidStack);
    }
}
