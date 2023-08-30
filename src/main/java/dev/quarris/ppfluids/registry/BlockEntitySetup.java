package dev.quarris.ppfluids.registry;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntitySetup {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModRef.ID);

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = REGISTRY.register("fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, BlockSetup.FLUID_PIPE.get()).build(null));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
