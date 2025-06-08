package dev.quarris.ppfluids.item;

import dev.quarris.ppfluids.registry.ItemSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class FluidItem extends Item {

    public FluidItem() {
        super(new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("info.ppfluids.fluid_item.usage"));
        FluidStack fluidStack = getFluidCopyFromItem(stack);
        tooltip.add(fluidStack.getHoverName().copy()
            .append(": ").append(String.valueOf(fluidStack.getAmount())));
    }

    public static ItemStack createItemFromFluid(FluidStack fluid) {
        ItemStack item = new ItemStack(ItemSetup.FLUID_HOLDER.get());
        FluidUtil.getFluidHandler(item).ifPresent(tank -> tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE));
        return item;
    }

    public static FluidStack getFluidCopyFromItem(ItemStack item) {
        return FluidUtil.getFluidHandler(item).map(handler -> handler.getFluidInTank(0)).orElse(FluidStack.EMPTY).copy();
    }

    public static ItemStack insertFluid(IFluidHandler handler, ItemStack fluidItem) {
        FluidStack fluidStack = FluidItem.getFluidCopyFromItem(fluidItem);
        int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) {
            return fluidItem;
        }

        filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        fluidStack.shrink(filled);

        if (fluidStack.isEmpty())
            return ItemStack.EMPTY;

        return FluidItem.createItemFromFluid(fluidStack);
    }
}
