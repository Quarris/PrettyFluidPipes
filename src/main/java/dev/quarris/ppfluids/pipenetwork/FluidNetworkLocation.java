package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.misc.ItemEquality;
import de.ellpeck.prettypipes.network.NetworkLocation;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FluidNetworkLocation extends NetworkLocation {

    private Map<Integer, FluidStack> fluidCache;
    private IFluidHandler handlerCache;

    public FluidNetworkLocation(BlockPos pipePos, Direction direction) {
        super(pipePos, direction);
    }

    public FluidNetworkLocation(CompoundNBT nbt) {
        super(nbt);
    }

    public List<Integer> getFluidSlots(World world, FluidStack fluid) {
        if (this.isEmpty(world))
            return Collections.emptyList();
        return this.getFluids(world).entrySet().stream()
                .filter(kv -> kv.getValue().isFluidEqual(fluid) && this.canExtract(world, kv.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public int getFluidAmount(World world, FluidStack fluid) {
        if (this.isEmpty(world))
            return 0;
        return this.getFluids(world).entrySet().stream()
                .filter(kv -> kv.getValue().isFluidEqual(fluid) && this.canExtract(world, kv.getKey()))
                .mapToInt(kv -> kv.getValue().getAmount()).sum();
    }

    public Map<Integer, FluidStack> getFluids(World world) {
        if (this.fluidCache == null) {
            IFluidHandler handler = this.getFluidHandler(world);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack stack = handler.getFluidInTank(i);
                    if (stack.isEmpty())
                        continue;
                    if (this.fluidCache == null)
                        this.fluidCache = new HashMap<>();
                    this.fluidCache.put(i, stack);
                }
            }
        }
        return this.fluidCache;
    }

    public IFluidHandler getFluidHandler(World world) {
        if (this.handlerCache == null) {
            PipeNetwork network = PipeNetwork.get(world);
            PipeTileEntity pipe = network.getPipe(this.pipePos);
            if (!(pipe instanceof FluidPipeTileEntity))
                throw new IllegalArgumentException(String.format("Pipe at %s is not a Fluid Pipe for a FluidNetworkLocation instance. Please report to PrettyPipesFluids issues.", this.pipePos));
            this.handlerCache = ((FluidPipeTileEntity) pipe).getAdjacentFluidHandler(this.direction);
        }
        return this.handlerCache;
    }

    @Override
    public boolean canExtract(World world, int slot) {
        IFluidHandler handler = this.getFluidHandler(world);
        if (handler == null)
            return false;
        FluidStack stored = handler.getFluidInTank(slot).copy();
        stored.setAmount(1);
        return !handler.drain(stored, IFluidHandler.FluidAction.SIMULATE).isEmpty();
    }

    @Override
    public boolean isEmpty(World world) {
        Map<Integer, FluidStack> fluids = this.getFluids(world);
        return fluids == null || fluids.isEmpty();
    }

    @Override
    public IItemHandler getItemHandler(World world) {
        throw new UnsupportedOperationException("getItemHandler is unsupported for FluidNetworkLocation");
    }

    @Override
    public Map<Integer, ItemStack> getItems(World world) {
        throw new UnsupportedOperationException("getItems is unsupported for FluidNetworkLocation");
    }

    @Override
    public List<Integer> getStackSlots(World world, ItemStack stack, ItemEquality... equalityTypes) {
        throw new UnsupportedOperationException("getStackSlots is unsupported for FluidNetworkLocation");
    }

    @Override
    public int getItemAmount(World world, ItemStack stack, ItemEquality... equalityTypes) {
        throw new UnsupportedOperationException("getItemAmount is unsupported for FluidNetworkLocation");
    }
}
