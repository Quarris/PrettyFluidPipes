package dev.quarris.ppfluids.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.ItemFluidContainer;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import dev.quarris.ppfluids.ModContent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidItem extends ItemFluidContainer {

    public FluidItem() {
        super(new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1), Integer.MAX_VALUE);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("info.ppfluids.fluid_item.usage"));
        FluidStack fluidStack = getFluidCopyFromItem(stack);
        tooltip.add(new TranslationTextComponent(fluidStack.getTranslationKey())
                .appendString(": ").appendString(String.valueOf(fluidStack.getAmount())));
    }

    public static ItemStack createItemFromFluid(FluidStack fluid, boolean simulate) {
        ItemStack item =  new ItemStack(ModContent.FLUID_ITEM);
        int filled = FluidUtil.getFluidHandler(item).map(tank -> tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE)).orElse(0);
        if (!simulate)
            fluid.grow(filled);
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

        return FluidItem.createItemFromFluid(fluidStack, false);
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidHandlerItemStack.Consumable(stack, capacity) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return this.getFluid().isEmpty();
            }
        };
    }
}
