package dev.quarris.ppfluids.item;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public interface IFluidModule extends IModule {

    int getMaxInsertionAmount(ItemStack module, PipeBlockEntity pipe, FluidStack fluidStack, IFluidHandler tank);

    boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler tank);

    boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IFluidHandler tank);

    @Override
    default boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IItemHandler storage) {
        return false;
    }

    @Override
    default boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IItemHandler storage) {
        return false;
    }

    @Override
    default int getMaxInsertionAmount(ItemStack itemStack, PipeBlockEntity pipeBlockEntity, ItemStack itemStack1, IItemHandler storage) {
        return 0;
    }
}
