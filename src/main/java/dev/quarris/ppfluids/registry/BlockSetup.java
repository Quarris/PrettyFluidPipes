package dev.quarris.ppfluids.registry;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.pipe.FluidPipeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockSetup {

    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModRef.ID);


    public static final RegistryObject<Block> FLUID_PIPE = REGISTRY.register("fluid_pipe", FluidPipeBlock::new);

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
