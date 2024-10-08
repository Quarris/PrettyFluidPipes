package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.network.NetworkLock;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.item.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipeNetworkUtil {

    public static FluidStack routeFluid(Level level, BlockPos inputPipePos, BlockPos inputTankPos, FluidStack fluid, BiFunction<ItemStack, Float, FluidPipeItem> itemSupplier, boolean preventOversending) {
        PipeNetwork network = PipeNetwork.get(level);
        if (!network.isNode(inputPipePos)) {
            return fluid;
        }
        if (!level.isLoaded(inputPipePos)) {
            return fluid;
        }

        PipeBlockEntity inputPipe = network.getPipe(inputPipePos);
        if (inputPipe == null)
            return fluid;

        network.startProfile("find_fluid_destination");
        List<BlockPos> nodes = network.getOrderedNetworkNodes(inputPipePos).stream()
            .filter(level::isLoaded)
            .filter(pos -> level.getBlockEntity(pos) instanceof FluidPipeBlockEntity)
            .collect(Collectors.toList());

        for (int i = 0; i < nodes.size(); ++i) {
            BlockPos pipePos = nodes.get(inputPipe.getNextNode(nodes, i));
            if (!level.isLoaded(pipePos)) {
                continue;
            }

            FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) network.getPipe(pipePos);
            Pair<BlockPos, ItemStack> dest = pipe.getAvailableDestination(Direction.values(), fluid.copy(), false, preventOversending);
            if (dest == null || dest.getLeft().equals(inputTankPos)) {
                continue;
            }

            Function<Float, IPipeItem> sup = (speed) -> itemSupplier.apply(dest.getRight(), speed);
            if (!network.routeItemToLocation(inputPipePos, inputTankPos, pipe.getBlockPos(), dest.getLeft(), dest.getRight(), sup)) {
                continue;
            }

            FluidStack remain = fluid.copy();
            FluidStack routedFluid = FluidItem.getFluidCopyFromItem(dest.getRight());
            remain.shrink(routedFluid.getAmount());
            network.endProfile();
            return remain;
        }

        network.endProfile();
        return fluid;
    }

    public static FluidStack requestFluid(Level level, BlockPos destPipe, BlockPos destInventory, FluidStack fluid) {
        PipeNetwork network = PipeNetwork.get(level);
        FluidStack remain = fluid.copy();
        // check existing items
        for (FluidNetworkLocation location : getOrderedNetworkFluids(level, destPipe)) {
            remain = requestExistingFluid(level, location, destPipe, destInventory, null, remain);
            if (remain.isEmpty())
                return remain;
        }
        // check craftable items
        return FluidStack.EMPTY;
    }

    public static FluidStack requestExistingFluid(Level level, FluidNetworkLocation location, BlockPos destPipe, BlockPos destInventory, NetworkLock ignoredLock, FluidStack requestedFluid) {
        PipeNetwork network = PipeNetwork.get(level);
        if (location.getPos().equals(destInventory))
            return requestedFluid;
        // make sure we don't pull any locked items
        int stored = location.getFluidAmount(level, requestedFluid);
        if (stored <= 0)
            return requestedFluid;
        // make sure we only extract less than or equal to the requested amount
        int toExtract = stored;
        if (requestedFluid.getAmount() < stored) {
            toExtract = requestedFluid.getAmount();
        }
        FluidStack leftOver = requestedFluid.copy();
        leftOver.shrink(toExtract);
        for (int slot : location.getFluidSlots(level, requestedFluid)) {
            // try to extract from that location's inventory and send the item
            IFluidHandler handler = location.getFluidHandler(level);
            FluidStack stack = handler.getFluidInTank(slot).copy();
            stack.setAmount(toExtract);
            FluidStack extracted = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
            ItemStack fluidItem = FluidItem.createItemFromFluid(extracted);
            if (network.routeItemToLocation(location.pipePos, location.getPos(), destPipe, destInventory, fluidItem, speed -> new FluidPipeItem(fluidItem, speed))) {
                handler.drain(extracted, IFluidHandler.FluidAction.EXECUTE);
                toExtract -= extracted.getAmount();
                if (toExtract <= 0)
                    break;
            }
        }
        return leftOver;
    }

    public static List<FluidNetworkLocation> getOrderedNetworkFluids(Level level, BlockPos node) {
        PipeNetwork network = PipeNetwork.get(level);
        if (!network.isNode(node))
            return Collections.emptyList();
        network.startProfile("get_network_fluids");
        List<FluidNetworkLocation> info = new ArrayList<>();
        for (BlockPos dest : network.getOrderedNetworkNodes(node)) {
            if (!level.isLoaded(dest))
                continue;
            PipeBlockEntity block = network.getPipe(dest);
            if (!(block instanceof FluidPipeBlockEntity pipe))
                continue;

            for (Direction dir : Direction.values()) {
                IFluidHandler handler = pipe.getFluidHandler(dir);
                if (handler == null)
                    continue;

                if (!pipe.canNetworkSee(dir, handler))
                    continue;

                // check if this handler already exists (double-connected pipes, double chests etc.)
                if (info.stream().anyMatch(l -> handler.equals(l.getFluidHandler(level))))
                    continue;
                FluidNetworkLocation location = new FluidNetworkLocation(dest, dir);
                if (!location.isEmpty(level))
                    info.add(location);
            }
        }
        network.endProfile();
        return info;
    }
}
