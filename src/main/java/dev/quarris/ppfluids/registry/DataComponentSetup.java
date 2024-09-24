package dev.quarris.ppfluids.registry;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.misc.FluidFilter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DataComponentSetup {

    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModRef.ID);

    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID_CONTENT_DATA = REGISTRY.register("fluid_content", () -> DataComponentType.<SimpleFluidContent>builder().persistent(SimpleFluidContent.CODEC).cacheEncoding().build());
    public static final Supplier<DataComponentType<FluidFilter.FilterData>> FLUID_FILTER_DATA =  REGISTRY.register("fluid_filter", () -> DataComponentType.<FluidFilter.FilterData>builder().persistent(FluidFilter.FilterData.CODEC).cacheEncoding().build());

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

}
