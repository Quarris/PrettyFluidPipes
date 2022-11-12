package dev.quarris.ppfluids.container;

import dev.quarris.ppfluids.misc.FluidFilter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FluidFilterSlot extends SlotItemHandler {

    private final int index;

    public FluidFilterSlot(IItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.index = index;
    }

    public static boolean clickFilter(AbstractContainerMenu container, int slotId, Player player) {
        if (slotId >= 0 && slotId < container.slots.size()) {
            Slot slot = container.getSlot(slotId);
            if (slot instanceof FluidFilterSlot) {
                ((FluidFilterSlot)slot).slotClick(container);
                return true;
            }
        }

        return false;
    }

    private void slotClick(AbstractContainerMenu menu) {
        ItemStack heldStack = menu.getCarried();
        ItemStack stackInSlot = this.getItem();
        if (!stackInSlot.isEmpty() && heldStack.isEmpty()) {
            this.set(ItemStack.EMPTY);
        } else if (!heldStack.isEmpty()) {
            FluidStack fluid = FluidUtil.getFluidContained(heldStack).orElse(FluidStack.EMPTY).copy();
            if (!fluid.isEmpty()) {
                ItemStack bucket = FluidUtil.getFilledBucket(fluid);
                this.set(bucket);
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }


    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }
}
