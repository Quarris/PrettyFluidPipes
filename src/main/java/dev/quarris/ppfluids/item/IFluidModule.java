package dev.quarris.ppfluids.item;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public interface IFluidModule extends IModule {

    boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler destination);

    boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IFluidHandler destination);

    int getMaxInsertionAmount(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler destination);

    default boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IItemHandler destination) {
        return false;
    }

    default boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IItemHandler destination) {
        return false;
    }

    default int getMaxInsertionAmount(ItemStack module, PipeBlockEntity pipe, Direction dir, IItemHandler destination) {
        return 0;
    }


}
