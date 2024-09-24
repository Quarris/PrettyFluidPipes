package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.item.*;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ItemSetup {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.Items.createItems(ModRef.ID);

    public static final Supplier<FluidItem> FLUID_HOLDER = REGISTRY.register("fluid_item", FluidItem::new);

    static {
        REGISTRY.registerSimpleBlockItem(BlockSetup.FLUID_PIPE);
        REGISTRY.registerItem("fluid_limiter_module", props -> new FluidLimiterModuleItem("fluid_limiter_module", props), new Item.Properties());

        registerTieredModule(REGISTRY, "fluid_extraction_module", new Item.Properties(), FluidExtractionModuleItem::new);
        registerTieredModule(REGISTRY, "fluid_filter_module", new Item.Properties(), FluidFilterModuleItem::new);
        registerTieredModule(REGISTRY, "fluid_retrieval_module", new Item.Properties(), FluidRetrievalModuleItem::new);

        IPipeItem.TYPES.put(FluidPipeItem.FLUID_TYPE, FluidPipeItem::new);
    }

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static void registerTieredModule(DeferredRegister<Item> registry, String name, Item.Properties properties, TriFunction<String, ModuleTier, Item.Properties, ModuleItem> item) {
        for (ModuleTier tier : ModuleTier.values()) {
            String moduleName = tier.name().toLowerCase(Locale.ROOT) + "_" + name;
            registry.register(moduleName, () -> item.apply(name, tier, properties));
        }
    }
}
