package quarris.ppfluids.items;

import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import quarris.ppfluids.network.FluidPipeItem;
import quarris.ppfluids.pipe.FluidPipeTileEntity;

public class FluidExtractionModuleItem extends ModuleItem {

    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    //public final int filterSlots;

    public FluidExtractionModuleItem(String name, ModuleTier tier) {
        super(name);
        this.maxExtraction = tier.forTier(500, 2000, 8000);
        this.speed = tier.forTier(20, 15, 10);
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
        //TODO FluidFilter
        //ItemFilter filter = new ItemFilter(this.filterSlots, module, tile);

        PipeNetwork network = PipeNetwork.get(tile.getWorld());
        for (Direction dir : Direction.values()) {
            IFluidHandler handler = fluidPipe.getFluidHandler(dir, null);
            if (handler == null)
                continue;

            FluidStack fluid = handler.drain(this.maxExtraction, IFluidHandler.FluidAction.SIMULATE);
            if (fluid.isEmpty())
                continue;
            //if (!filter.isAllowed(stack))
                //continue;
            ItemStack remain = network.routeItem(tile.getPos(), tile.getPos().offset(dir), FluidItem.createItemFromFluid(fluid), FluidPipeItem::new, this.preventOversending);
            FluidStack remainedFluid = FluidItem.createFluidFromItem(remain);
            if (remainedFluid.getAmount() != fluid.getAmount()) {
                handler.drain(fluid.getAmount() - remainedFluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                return;
            }
        }
    }

    @Override
    public boolean isCompatible(ItemStack itemStack, PipeTileEntity pipeTileEntity, IModule iModule) {
        return pipeTileEntity instanceof FluidPipeTileEntity && !(iModule instanceof FluidExtractionModuleItem);
    }

    public boolean canNetworkSee(ItemStack module, PipeTileEntity tile) {
        return false;
    }

    public boolean canAcceptItem(ItemStack module, PipeTileEntity tile, ItemStack stack) {
        return false;
    }

    public boolean hasContainer(ItemStack module, PipeTileEntity tile) {
        return false;
    }

    /* TODO Container for filters
    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeTileEntity tile, int windowId, PlayerInventory inv, PlayerEntity player, int moduleIndex) {
        return new ExtractionModuleContainer(Registry.extractionModuleContainer, windowId, player, tile.getPos(), moduleIndex);
    }
     */
}
