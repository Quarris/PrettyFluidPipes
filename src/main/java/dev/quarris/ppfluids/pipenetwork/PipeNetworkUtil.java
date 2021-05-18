package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.misc.ItemEquality;
import de.ellpeck.prettypipes.network.NetworkLocation;
import de.ellpeck.prettypipes.network.NetworkLock;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipeNetworkUtil {

    public static FluidStack routeFluid(World world, BlockPos inputPipePos, BlockPos inputTankPos, FluidStack fluid, BiFunction<ItemStack, Float, FluidPipeItem> itemSupplier, boolean preventOversending) {
        PipeNetwork network = PipeNetwork.get(world);
        if (!network.isNode(inputPipePos)) {
            return fluid;
        }
        if (!world.isBlockLoaded(inputPipePos)) {
            return fluid;
        }

        PipeTileEntity inputPipe = network.getPipe(inputPipePos);
        if (inputPipe == null)
            return fluid;

        network.startProfile("find_fluid_destination");
        List<BlockPos> nodes = network.getOrderedNetworkNodes(inputPipePos).stream()
                .filter(world::isBlockLoaded)
                .filter(pos -> world.getTileEntity(pos) instanceof FluidPipeTileEntity)
                .collect(Collectors.toList());

        for (int i = 0; i < nodes.size(); ++i) {
            BlockPos pipePos = nodes.get(inputPipe.getNextNode(nodes, i));
            if (world.isBlockLoaded(pipePos)) {
                FluidPipeTileEntity pipe = (FluidPipeTileEntity) network.getPipe(pipePos);
                Pair<BlockPos, ItemStack> dest = pipe.getAvailableDestination(fluid.copy(), false, preventOversending);
                if (dest != null && !dest.getLeft().equals(inputTankPos)) {
                    Function<Float, IPipeItem> sup = (speed) -> itemSupplier.apply(dest.getRight(), speed);
                    if (network.routeItemToLocation(inputPipePos, inputTankPos, pipe.getPos(), dest.getLeft(), dest.getRight(), sup)) {
                        FluidStack remain = fluid.copy();
                        FluidStack routedFluid = FluidItem.getFluidCopyFromItem(dest.getRight());
                        remain.shrink(routedFluid.getAmount());
                        network.endProfile();
                        return remain;
                    }
                }
            }
        }

        network.endProfile();
        return fluid;
    }

    public static FluidStack requestFluid(World world, BlockPos destPipe, BlockPos destInventory, FluidStack fluid) {
        PipeNetwork network = PipeNetwork.get(world);
        FluidStack remain = fluid.copy();
        // check existing items
        for (FluidNetworkLocation location : getOrderedNetworkFluids(world, destPipe)) {
            remain = requestExistingFluid(world, location, destPipe, destInventory, null, remain);
            if (remain.isEmpty())
                return remain;
        }
        // check craftable items
        return FluidStack.EMPTY;
    }

    public static FluidStack requestExistingFluid(World world, FluidNetworkLocation location, BlockPos destPipe, BlockPos destInventory, NetworkLock ignoredLock, FluidStack fluid) {
        PipeNetwork network = PipeNetwork.get(world);
        if (location.getPos().equals(destInventory))
            return fluid;
        // make sure we don't pull any locked items
        int amount = location.getFluidAmount(world, fluid);
        if (amount <= 0)
            return fluid;
        FluidStack remain = fluid.copy();
        // make sure we only extract less than or equal to the requested amount
        if (remain.getAmount() < amount)
            amount = remain.getAmount();
        remain.shrink(amount);
        for (int slot : location.getFluidSlots(world, fluid)) {
            // try to extract from that location's inventory and send the item
            IFluidHandler handler = location.getFluidHandler(world);
            FluidStack stack = handler.getFluidInTank(slot).copy();
            FluidStack extracted = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
            ItemStack fluidItem = FluidItem.createItemFromFluid(extracted, false);
            if (network.routeItemToLocation(location.pipePos, location.getPos(), destPipe, destInventory, fluidItem, speed -> new FluidPipeItem(fluidItem, speed))) {
                handler.drain(extracted, IFluidHandler.FluidAction.EXECUTE);
                amount -= extracted.getAmount();
                if (amount <= 0)
                    break;
            }
        }
        return remain;
    }

    public static List<FluidNetworkLocation> getOrderedNetworkFluids(World world, BlockPos node) {
        PipeNetwork network = PipeNetwork.get(world);
        if (!network.isNode(node))
            return Collections.emptyList();
        network.startProfile("get_network_fluids");
        List<FluidNetworkLocation> info = new ArrayList<>();
        for (BlockPos dest : network.getOrderedNetworkNodes(node)) {
            if (!world.isBlockLoaded(dest))
                continue;
            PipeTileEntity pipe = network.getPipe(dest);
            if (!(pipe instanceof FluidPipeTileEntity))
                continue;

            if (!pipe.canNetworkSee())
                continue;

            FluidPipeTileEntity fluidPipe = (FluidPipeTileEntity) pipe;
            for (Direction dir : Direction.values()) {
                IFluidHandler handler = fluidPipe.getAdjacentFluidHandler(dir);
                if (handler == null)
                    continue;
                // check if this handler already exists (double-connected pipes, double chests etc.)
                if (info.stream().anyMatch(l -> handler.equals(l.getFluidHandler(world))))
                    continue;
                FluidNetworkLocation location = new FluidNetworkLocation(dest, dir);
                if (!location.isEmpty(world))
                    info.add(location);
            }
        }
        network.endProfile();
        return info;
    }
}
