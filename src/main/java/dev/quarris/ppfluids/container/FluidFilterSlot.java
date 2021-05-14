package dev.quarris.ppfluids.container;

import dev.quarris.ppfluids.misc.FluidFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.SlotItemHandler;

public class FluidFilterSlot extends SlotItemHandler {

    private final FluidFilter filter;
    private final int index;

    public FluidFilterSlot(FluidFilter filter, int index, int xPosition, int yPosition) {
        super(filter, index, xPosition, yPosition);
        this.index = index;
        this.filter = filter;
    }

    public static boolean checkFilter(Container container, int slotId, PlayerEntity player) {
        if (slotId >= 0 && slotId < container.inventorySlots.size()) {
            Slot slot = container.getSlot(slotId);
            if (slot instanceof FluidFilterSlot) {
                ((FluidFilterSlot)slot).slotClick(player);
                return true;
            }
        }

        return false;
    }

    private void slotClick(PlayerEntity player) {
        ItemStack heldStack = player.inventory.getItemStack();
        ItemStack stackInSlot = this.getStack();
        if (!stackInSlot.isEmpty() && heldStack.isEmpty()) {
            this.putFluidStack(FluidStack.EMPTY);
        } else if (!heldStack.isEmpty()) {
            FluidStack fluid = FluidUtil.getFluidContained(heldStack).orElse(FluidStack.EMPTY).copy();
            if (!fluid.isEmpty()) {
                fluid.setAmount(1);
                this.putFluidStack(fluid);
            }
        }
    }

    public void putFluidStack(FluidStack stack) {
        this.filter.setFilter(this.index, stack);
        this.onSlotChanged();
    }

    @Override
    public void onSlotChanged() {
        this.filter.setModified(true);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return false;
    }
}
