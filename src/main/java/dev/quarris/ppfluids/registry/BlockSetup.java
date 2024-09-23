package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.Registry;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.pipe.FluidPipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockSetup {

    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.Blocks.createBlocks(ModRef.ID);

    public static final DeferredBlock<FluidPipeBlock> FLUID_PIPE = REGISTRY.register("fluid_pipe", () -> new FluidPipeBlock(BlockBehaviour.Properties.ofFullCopy(Registry.pipeBlock)));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
