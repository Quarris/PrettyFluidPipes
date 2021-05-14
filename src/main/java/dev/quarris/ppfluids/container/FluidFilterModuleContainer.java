package dev.quarris.ppfluids.container;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.items.FluidFilterModuleItem;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.misc.FluidFilter.IFluidFilteredContainer;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class FluidFilterModuleContainer extends AbstractPipeContainer<FluidFilterModuleItem> implements IFluidFilteredContainer {

    private FluidFilter filter;

    public FluidFilterModuleContainer(@Nullable ContainerType<?> type, int id, PlayerEntity player, BlockPos pos, int moduleIndex) {
        super(type, id, player, pos, moduleIndex);
    }

    @Override
    protected void addSlots() {
        this.filter = this.module.getFluidFilter(this.moduleStack, (FluidPipeTileEntity)this.tile);
        List<Slot> filterSlots = this.filter.createContainerSlots((176 - Math.min(this.module.filterSlots, 9) * 18) / 2 + 1, 49);
        for (Slot slot : filterSlots) {
            this.addSlot(slot);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.filter.save();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        return FluidFilterSlot.checkFilter(this, slotId, player) ? ItemStack.EMPTY : super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public FluidFilter getFilter() {
        return this.filter;
    }
}
