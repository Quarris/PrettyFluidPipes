package dev.quarris.ppfluids;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.items.FluidExtractionModuleItem;
import dev.quarris.ppfluids.items.FluidFilterModuleItem;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.items.FluidRetrievalModuleItem;
import dev.quarris.ppfluids.pipe.FluidPipeBlock;
import dev.quarris.ppfluids.pipe.FluidPipeBlockEntity;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContent {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModRef.ID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModRef.ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModRef.ID);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModRef.ID);

    // Blocks
    public static final RegistryObject<Block> FLUID_PIPE = BLOCKS.register("fluid_pipe", FluidPipeBlock::new);

    //Items
    public static final RegistryObject<Item> FLUID_ITEM = ITEMS.register("fluid_item", FluidItem::new);
    static {
        ITEMS.register(FLUID_PIPE.getId().getPath(), () -> new BlockItem(FLUID_PIPE.get(), new Item.Properties().tab(Registry.TAB)));
        registerTieredModule(ITEMS, "fluid_extraction_module", FluidExtractionModuleItem::new);
        registerTieredModule(ITEMS, "fluid_filter_module", FluidFilterModuleItem::new);
        registerTieredModule(ITEMS, "fluid_retrieval_module", FluidRetrievalModuleItem::new);
        IPipeItem.TYPES.put(FluidPipeItem.FLUID_TYPE, FluidPipeItem::new);
    }

    // Block Entities
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE_TILE = BLOCK_ENTITY_TYPES.register("fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, FLUID_PIPE.get()).build(null));

    // Menus
    public static final RegistryObject<MenuType<FluidFilterModuleContainer>> FLUID_FILTER_CONTAINER = MENU_TYPES.register("fluid_filter", ModContent::createPipeContainer);
    public static final RegistryObject<MenuType<FluidExtractionModuleContainer>> FLUID_EXTRACTION_CONTAINER = MENU_TYPES.register("fluid_extraction", ModContent::createPipeContainer);
    public static final RegistryObject<MenuType<FluidRetrievalModuleContainer>> FLUID_RETRIEVAL_CONTAINER = MENU_TYPES.register("fluid_retrieval", ModContent::createPipeContainer);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        MENU_TYPES.register(bus);
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

    private static void registerTieredModule(DeferredRegister<Item> registry, String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        for (ModuleTier tier : ModuleTier.values()) {
            String moduleName = tier.name().toLowerCase(Locale.ROOT) + "_" + name;
            registry.register(moduleName, () -> item.apply(name, tier));
        }
    }

    private static <T extends AbstractPipeContainer<?>> MenuType<T> createPipeContainer() {
        return (MenuType) IForgeMenuType.create((windowId, inv, data) -> {
            PipeBlockEntity tile = Utility.getBlockEntity(PipeBlockEntity.class, inv.player.level, data.readBlockPos());
            int moduleIndex = data.readInt();
            ItemStack moduleStack = tile.modules.getStackInSlot(moduleIndex);
            return ((IModule) moduleStack.getItem()).getContainer(moduleStack, tile, windowId, inv, inv.player, moduleIndex);
        });
    }
}
