package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.network.NetworkLock;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
            if (level.isLoaded(pipePos)) {
                FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) network.getPipe(pipePos);
                Pair<BlockPos, ItemStack> dest = pipe.getAvailableDestination(fluid.copy(), false, preventOversending);
                if (dest != null && !dest.getLeft().equals(inputTankPos)) {
                    Function<Float, IPipeItem> sup = (speed) -> itemSupplier.apply(dest.getRight(), speed);
                    if (network.routeItemToLocation(inputPipePos, inputTankPos, pipe.getBlockPos(), dest.getLeft(), dest.getRight(), sup)) {
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

    public static FluidStack requestExistingFluid(Level level, FluidNetworkLocation location, BlockPos destPipe, BlockPos destInventory, NetworkLock ignoredLock, FluidStack fluid) {
        PipeNetwork network = PipeNetwork.get(level);
        if (location.getPos().equals(destInventory))
            return fluid;
        // make sure we don't pull any locked items
        int amount = location.getFluidAmount(level, fluid);
        if (amount <= 0)
            return fluid;
        FluidStack remain = fluid.copy();
        // make sure we only extract less than or equal to the requested amount
        if (remain.getAmount() < amount)
            amount = remain.getAmount();
        remain.shrink(amount);
        for (int slot : location.getFluidSlots(level, fluid)) {
            // try to extract from that location's inventory and send the item
            IFluidHandler handler = location.getFluidHandler(level);
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

    public static List<FluidNetworkLocation> getOrderedNetworkFluids(Level level, BlockPos node) {
        PipeNetwork network = PipeNetwork.get(level);
        if (!network.isNode(node))
            return Collections.emptyList();
        network.startProfile("get_network_fluids");
        List<FluidNetworkLocation> info = new ArrayList<>();
        for (BlockPos dest : network.getOrderedNetworkNodes(node)) {
            if (!level.isLoaded(dest))
                continue;
            PipeBlockEntity pipe = network.getPipe(dest);
            if (!(pipe instanceof FluidPipeBlockEntity))
                continue;

            if (!pipe.canNetworkSee())
                continue;

            FluidPipeBlockEntity fluidPipe = (FluidPipeBlockEntity) pipe;
            for (Direction dir : Direction.values()) {
                IFluidHandler handler = fluidPipe.getAdjacentFluidHandler(dir);
                if (handler == null)
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
