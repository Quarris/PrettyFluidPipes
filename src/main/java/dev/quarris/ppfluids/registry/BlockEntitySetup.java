package dev.quarris.ppfluids.registry;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntitySetup {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModRef.ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = REGISTRY.register("fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, BlockSetup.FLUID_PIPE.get()).build(null));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
