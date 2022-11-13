package old.dev.quarris.ppfluids.container;

import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import old.dev.quarris.ppfluids.items.FluidExtractionModuleItem;
import old.dev.quarris.ppfluids.misc.FluidFilter;
import old.dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;

public class FluidExtractionModuleContainer extends AbstractPipeContainer<FluidExtractionModuleItem> implements FluidFilter.IFluidFilteredContainer, DirectionSelector.IDirectionContainer {

    private FluidFilter filter;
    private DirectionSelector directionSelector;

    public FluidExtractionModuleContainer(@Nullable MenuType<?> type, int id, Player player, BlockPos pos, int moduleIndex) {
        super(type, id, player, pos, moduleIndex);
    }

    @Override
    protected void addSlots() {
        this.filter = this.module.getFluidFilter(this.moduleStack, (FluidPipeBlockEntity) this.tile);
        this.directionSelector = this.module.getDirectionSelector(this.moduleStack, this.tile);
        for (Slot slot : this.filter.createSlots((176 - this.module.filterSlots * 18) / 2 + 1, 17 + 32)) {
            this.addSlot(slot);
        }
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (FluidFilterSlot.clickFilter(this, slotId, player))
            return;

        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.filter.save();
        this.directionSelector.save();
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
