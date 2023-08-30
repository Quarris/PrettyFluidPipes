package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.item.FluidExtractionModuleItem;
import dev.quarris.ppfluids.item.FluidFilterModuleItem;
import dev.quarris.ppfluids.item.FluidItem;
import dev.quarris.ppfluids.item.FluidRetrievalModuleItem;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.function.BiFunction;

public class ItemSetup {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModRef.ID);

    public static final RegistryObject<Item> FLUID_HOLDER = REGISTRY.register("fluid_item", FluidItem::new);
    static {
        REGISTRY.register(BlockSetup.FLUID_PIPE.getId().getPath(), () -> new BlockItem(BlockSetup.FLUID_PIPE.get(), new Item.Properties().tab(Registry.TAB)));
        registerTieredModule(REGISTRY, "fluid_extraction_module", FluidExtractionModuleItem::new);
        registerTieredModule(REGISTRY, "fluid_filter_module", FluidFilterModuleItem::new);
        registerTieredModule(REGISTRY, "fluid_retrieval_module", FluidRetrievalModuleItem::new);
        IPipeItem.TYPES.put(FluidPipeItem.FLUID_TYPE, FluidPipeItem::new);
    }

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

    private static void registerTieredModule(DeferredRegister<Item> registry, String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        for (ModuleTier tier : ModuleTier.values()) {
            String moduleName = tier.name().toLowerCase(Locale.ROOT) + "_" + name;
            registry.register(moduleName, () -> item.apply(name, tier));
        }
    }
    
    /*private static Item[] createTieredModule(String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        Item[] items = new Item[ModuleTier.values().length];
        int i = 0;
        for (ModuleTier tier : ModuleTier.values()) {
            items[i] = item.apply(name, tier).setRegistryName(ModRef.ID, tier.name().toLowerCase(Locale.ROOT) + "_" + name);
            i++;
        }
        return items;
    }*/
    
}
