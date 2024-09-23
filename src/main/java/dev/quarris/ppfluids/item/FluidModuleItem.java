package dev.quarris.ppfluids.item;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidModuleItem extends ModuleItem implements IFluidModule {

    public FluidModuleItem(String name) {
        super(name);
    }

    @Override
    public boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler destination) {
        return true;
    }

    @Override
    public boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IFluidHandler destination) {
        return true;
    }

    @Override
    public int getMaxInsertionAmount(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler destination) {
        return 0;
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeBlockEntity pipeBlockEntity, IModule iModule) {
        return false;
    }

    @Override
    public boolean hasContainer(ItemStack itemStack, PipeBlockEntity pipeBlockEntity) {
        return false;
    }
}
