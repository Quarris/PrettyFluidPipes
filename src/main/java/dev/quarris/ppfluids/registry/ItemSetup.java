package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.item.*;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class ItemSetup {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.Items.createItems(ModRef.ID);

    public static final Supplier<FluidItem> FLUID_HOLDER = REGISTRY.register("fluid_item", FluidItem::new);
    public static final DeferredItem<FluidLimiterModuleItem> LIMITER_MODULE = REGISTRY.registerItem("fluid_limiter_module", props -> new FluidLimiterModuleItem("fluid_limiter_module", props), new Item.Properties());

    public static final Map<ModuleTier, DeferredItem<ModuleItem>> EXTRACTION_MODULES = new EnumMap<>(ModuleTier.class);
    public static final Map<ModuleTier, DeferredItem<ModuleItem>> FILTER_MODULES = new EnumMap<>(ModuleTier.class);
    public static final Map<ModuleTier, DeferredItem<ModuleItem>> RETRIEVAL_MODULES = new EnumMap<>(ModuleTier.class);

    static {
        REGISTRY.registerSimpleBlockItem(BlockSetup.FLUID_PIPE);

        registerTieredModule(EXTRACTION_MODULES, "fluid_extraction_module", new Item.Properties(), FluidExtractionModuleItem::new);
        registerTieredModule(FILTER_MODULES, "fluid_filter_module", new Item.Properties(), FluidFilterModuleItem::new);
        registerTieredModule(RETRIEVAL_MODULES, "fluid_retrieval_module", new Item.Properties(), FluidRetrievalModuleItem::new);

        IPipeItem.TYPES.put(FluidPipeItem.FLUID_TYPE, FluidPipeItem::new);
    }

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static void registerTieredModule(Map<ModuleTier, DeferredItem<ModuleItem>> registryMap, String name, Item.Properties properties, TriFunction<String, ModuleTier, Item.Properties, ModuleItem> item) {
        for (ModuleTier tier : ModuleTier.values()) {
            String moduleName = tier.name().toLowerCase(Locale.ROOT) + "_" + name;
            var module = ItemSetup.REGISTRY.registerItem(moduleName, props -> item.apply(name, tier, props), properties);
            registryMap.put(tier, module);

        }
    }
}
