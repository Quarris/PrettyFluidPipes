package dev.quarris.ppfluids.container;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.items.FluidRetrievalModuleItem;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.List;

public class FluidRetrievalModuleContainer extends AbstractPipeContainer<FluidRetrievalModuleItem> implements FluidFilter.IFluidFilteredContainer, DirectionSelector.IDirectionContainer {

    private FluidFilter filter;
    private DirectionSelector directionSelector;

    public FluidRetrievalModuleContainer(@Nullable MenuType<?> type, int id, Player player, BlockPos pos, int moduleIndex) {
        super(type, id, player, pos, moduleIndex);
    }

    @Override
    protected void addSlots() {
        this.filter = this.module.getFluidFilter(this.moduleStack, (FluidPipeBlockEntity) this.tile);
        this.directionSelector = this.module.getDirectionSelector(this.moduleStack, this.tile);
        List<Slot> filterSlots = this.filter.createContainerSlots((176 - Math.min(this.module.filterSlots, 9) * 18) / 2 + 1, 49);
        for (Slot slot : filterSlots) {
            this.addSlot(slot);
        }
    }

    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.filter.save();
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (!FluidFilterSlot.isFilterSlot(this, slotId, player)) {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public FluidFilter getFilter() {
        return this.filter;
    }

    @Override
    public DirectionSelector getSelector() {
        return this.directionSelector;
    }
}
