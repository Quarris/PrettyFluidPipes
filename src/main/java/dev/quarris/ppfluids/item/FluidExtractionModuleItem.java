package dev.quarris.ppfluids.item;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.misc.DirectionSelector;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.container.FluidExtractionContainer;
import dev.quarris.ppfluids.misc.FluidDirectionSelector;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidExtractionModuleItem extends FluidModuleItem implements IFluidFilterProvider {

    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    public final int filterSlots;

    public FluidExtractionModuleItem(String name, ModuleTier tier, Item.Properties properties) {
        super(name, properties);
        this.maxExtraction = tier.forTier(500, 2000, 8000);
        this.speed = tier.forTier(40, 20, 10);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.preventOversending = true;
    }

    @Override
    public void tick(ItemStack module, PipeBlockEntity pipe) {
        if (!(pipe instanceof FluidPipeBlockEntity fluidPipe) || !pipe.shouldWorkNow(this.speed) || !pipe.canWork())
            return;

        FluidFilter filter = this.getFluidFilter(module, fluidPipe);
        DirectionSelector dirSelector = this.getDirectionSelector(module, fluidPipe);
        for (Direction dir : dirSelector.directions()) {
            IFluidHandler tank = fluidPipe.getFluidHandler(dir);
            if (tank == null)
                continue;

            FluidStack toExtract = tank.drain(this.maxExtraction, IFluidHandler.FluidAction.SIMULATE);
            if (toExtract.isEmpty())
                continue;

            if (!filter.isPipeFluidAllowed(toExtract))
                continue;

            FluidStack remainder = PipeNetworkUtil.routeFluid(fluidPipe.getLevel(), fluidPipe.getBlockPos(), fluidPipe.getBlockPos().relative(dir), toExtract, FluidPipeItem::new, this.preventOversending);

            if (remainder.getAmount() != toExtract.getAmount()) {
                tank.drain(toExtract.getAmount() - remainder.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    @Override
    public boolean canNetworkSee(ItemStack module, PipeBlockEntity pipe, Direction dir, IFluidHandler tank) {
        return !this.getDirectionSelector(module, pipe).has(dir);
    }

    @Override
    public boolean canAcceptItem(ItemStack module, PipeBlockEntity pipe, ItemStack stack, Direction dir, IFluidHandler tank) {
        return !this.getDirectionSelector(module, pipe).has(dir);
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeBlockEntity tile, IModule iModule) {
        return tile instanceof FluidPipeBlockEntity && !(iModule instanceof FluidExtractionModuleItem);
    }

    @Override
    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return tile instanceof FluidPipeBlockEntity;
    }

    @Override
    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidExtractionContainer(MenuSetup.FLUID_EXTRACTION.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    @Override
    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        return new FluidFilter(this.filterSlots, module, tile);
    }

    @Override
    public DirectionSelector getDirectionSelector(ItemStack module, PipeBlockEntity tile) {
        return new FluidDirectionSelector(module, tile);
    }
}
