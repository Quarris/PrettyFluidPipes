package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;

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
}
