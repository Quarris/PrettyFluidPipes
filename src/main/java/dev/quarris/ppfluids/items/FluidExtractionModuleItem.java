package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidExtractionModuleItem extends ModuleItem implements IFluidFilterProvider {

    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    public final int filterSlots;

    public FluidExtractionModuleItem(String name, ModuleTier tier) {
        super(name);
        this.maxExtraction = tier.forTier(500, 2000, 8000);
        this.speed = tier.forTier(20, 15, 10);
        this.filterSlots = tier.forTier(2, 4, 8);
        this.preventOversending = tier.forTier(true, true, true);
    }

    @Override
    public void tick(ItemStack module, PipeBlockEntity tile) {
        if (tile.getLevel().getGameTime() % this.speed != 0)
            return;
        if (!tile.canWork())
            return;
        if (!(tile instanceof FluidPipeBlockEntity))
            return;

        FluidPipeBlockEntity fluidPipe = (FluidPipeBlockEntity) tile;
        FluidFilter filter = new FluidFilter(this.filterSlots, module, fluidPipe);

        for (Direction dir : Direction.values()) {
            IFluidHandler tank = fluidPipe.getAdjacentFluidHandler(dir);
            if (tank == null)
                continue;

            FluidStack fluid = tank.drain(this.maxExtraction, IFluidHandler.FluidAction.SIMULATE);
            if (fluid.isEmpty())
                continue;

            if (!filter.isAllowed(fluid))
                continue;

            FluidStack remain = PipeNetworkUtil.routeFluid(fluidPipe.getLevel(), fluidPipe.getBlockPos(), fluidPipe.getBlockPos().relative(dir), fluid, FluidPipeItem::new, this.preventOversending);

            if (remain.getAmount() != fluid.getAmount()) {
                tank.drain(fluid.getAmount() - remain.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeBlockEntity tile, IModule iModule) {
        return tile instanceof FluidPipeBlockEntity && !(iModule instanceof FluidExtractionModuleItem);
    }

    public boolean canNetworkSee(ItemStack module, PipeBlockEntity tile) {
        return false;
    }

    public boolean canAcceptItem(ItemStack module, PipeBlockEntity tile, ItemStack stack) {
        return false;
    }

    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return tile instanceof FluidPipeBlockEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FluidExtractionModuleContainer(ModContent.FLUID_EXTRACTION_CONTAINER.get(), windowId, player, tile.getBlockPos(), moduleIndex);
    }

    public FluidFilter getFluidFilter(ItemStack module, FluidPipeBlockEntity tile) {
        return new FluidFilter(this.filterSlots, module, tile);
    }
}
