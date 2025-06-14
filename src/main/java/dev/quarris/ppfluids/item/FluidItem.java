package dev.quarris.ppfluids.item;

import dev.quarris.ppfluids.registry.ItemSetup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.ItemFluidContainer;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidItem extends ItemFluidContainer {

    public FluidItem() {
        super(new Properties(), Integer.MAX_VALUE);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("info.ppfluids.fluid_item.usage"));
        FluidStack fluidStack = getFluidCopyFromItem(stack);
        tooltip.add(Component.translatable(fluidStack.getTranslationKey())
                .append(": ").append(String.valueOf(fluidStack.getAmount())));
    }

    public static ItemStack createItemFromFluid(FluidStack fluid) {
        ItemStack item =  new ItemStack(ItemSetup.FLUID_HOLDER.get());
        int filled = FluidUtil.getFluidHandler(item).map(tank -> tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE)).orElse(0);
        return item;
    }

    public static FluidStack getFluidCopyFromItem(ItemStack item) {
       return FluidUtil.getFluidHandler(item).map(handler -> handler.getFluidInTank(0).copy()).orElse(FluidStack.EMPTY);
    }

    public static ItemStack insertFluid(IFluidHandler handler, ItemStack fluidItem, boolean simulate) {
        FluidStack fluidStack = FluidItem.getFluidCopyFromItem(fluidItem);
        int filled = handler.fill(fluidStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        fluidStack.shrink(filled);

        if (fluidStack.isEmpty())
            return ItemStack.EMPTY;

        return FluidItem.createItemFromFluid(fluidStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack.Consumable(stack, capacity) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return this.getFluid().isEmpty();
            }
        };
    }
}
