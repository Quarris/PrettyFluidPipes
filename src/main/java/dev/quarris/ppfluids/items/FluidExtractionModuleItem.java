package dev.quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.ModContent;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import dev.quarris.ppfluids.pipenetwork.PipeNetworkUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
        this.preventOversending = tier.forTier(false, false, true);
    }

    @Override
    public void tick(ItemStack module, PipeTileEntity tile) {
        if (tile.getWorld().getGameTime() % this.speed != 0)
            return;
        if (!tile.canWork())
            return;
        if (!(tile instanceof FluidPipeTileEntity))
            return;

        FluidPipeTileEntity fluidPipe = (FluidPipeTileEntity) tile;
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

            FluidStack remain = PipeNetworkUtil.routeFluid(fluidPipe.getWorld(), fluidPipe.getPos(), fluidPipe.getPos().offset(dir), fluid, FluidPipeItem::new, this.preventOversending);

            if (remain.getAmount() != fluid.getAmount()) {
                tank.drain(fluid.getAmount() - remain.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeTileEntity pipeTileEntity, IModule iModule) {
        return !(iModule instanceof FluidExtractionModuleItem);
    }

    public boolean canNetworkSee(ItemStack module, PipeTileEntity tile) {
        return false;
    }

    public boolean canAcceptItem(ItemStack module, PipeTileEntity tile, ItemStack stack) {
        return false;
    }

    public boolean hasContainer(ItemStack module, PipeTileEntity tile) {
        return tile instanceof FluidPipeTileEntity;
    }

    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeTileEntity tile, int windowId, PlayerInventory inv, PlayerEntity player, int moduleIndex) {
        return new FluidExtractionModuleContainer(ModContent.FLUID_EXTRACTION_CONTAINER, windowId, player, tile.getPos(), moduleIndex);
    }

    public FluidFilter getFluidFilter(ItemStack module, FluidPipeTileEntity tile) {
        return new FluidFilter(this.filterSlots, module, tile);
    }
}
