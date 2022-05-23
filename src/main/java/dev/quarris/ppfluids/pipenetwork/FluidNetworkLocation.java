package dev.quarris.ppfluids.pipenetwork;

import de.ellpeck.prettypipes.misc.ItemEquality;
import de.ellpeck.prettypipes.network.NetworkLocation;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    public FluidNetworkLocation(CompoundTag nbt) {
        super(nbt);
    }

    public List<Integer> getFluidSlots(Level level, FluidStack fluid) {
        if (this.isEmpty(level))
            return Collections.emptyList();
        return this.getFluids(level).entrySet().stream()
                .filter(kv -> kv.getValue().isFluidEqual(fluid) && this.canExtract(level, kv.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public int getFluidAmount(Level level, FluidStack fluid) {
        if (this.isEmpty(level))
            return 0;
        return this.getFluids(level).entrySet().stream()
                .filter(kv -> kv.getValue().isFluidEqual(fluid) && this.canExtract(level, kv.getKey()))
                .mapToInt(kv -> kv.getValue().getAmount()).sum();
    }

    public Map<Integer, FluidStack> getFluids(Level level) {
        if (this.fluidCache == null) {
            IFluidHandler handler = this.getFluidHandler(level);
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

    public IFluidHandler getFluidHandler(Level level) {
        if (this.handlerCache == null) {
            PipeNetwork network = PipeNetwork.get(level);
            PipeBlockEntity pipe = network.getPipe(this.pipePos);
            if (!(pipe instanceof FluidPipeBlockEntity))
                throw new IllegalArgumentException(String.format("Pipe at %s is not a Fluid Pipe for a FluidNetworkLocation instance. Please report to PrettyPipesFluids issues.", this.pipePos));
            this.handlerCache = ((FluidPipeBlockEntity) pipe).getAdjacentFluidHandler(this.direction);
        }
        return this.handlerCache;
    }

    @Override
    public boolean canExtract(Level level, int slot) {
        IFluidHandler handler = this.getFluidHandler(level);
        if (handler == null)
            return false;
        FluidStack stored = handler.getFluidInTank(slot).copy();
        stored.setAmount(1);
        return !handler.drain(stored, IFluidHandler.FluidAction.SIMULATE).isEmpty();
    }

    @Override
    public boolean isEmpty(Level level) {
        Map<Integer, FluidStack> fluids = this.getFluids(level);
        return fluids == null || fluids.isEmpty();
    }

    @Override
    public IItemHandler getItemHandler(Level le) {
        throw new UnsupportedOperationException("getItemHandler is unsupported for FluidNetworkLocation");
    }

    @Override
    public Map<Integer, ItemStack> getItems(Level level) {
        throw new UnsupportedOperationException("getItems is unsupported for FluidNetworkLocation");
    }

    @Override
    public List<Integer> getStackSlots(Level level, ItemStack stack, ItemEquality... equalityTypes) {
        throw new UnsupportedOperationException("getStackSlots is unsupported for FluidNetworkLocation");
    }

    @Override
    public int getItemAmount(Level level, ItemStack stack, ItemEquality... equalityTypes) {
        throw new UnsupportedOperationException("getItemAmount is unsupported for FluidNetworkLocation");
    }
}
