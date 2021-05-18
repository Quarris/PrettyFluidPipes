package dev.quarris.ppfluids;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.Utility;
import de.ellpeck.prettypipes.items.IModule;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import de.ellpeck.prettypipes.pipe.containers.AbstractPipeContainer;
import dev.quarris.ppfluids.container.FluidExtractionModuleContainer;
import dev.quarris.ppfluids.container.FluidFilterModuleContainer;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.items.FluidFilterModuleItem;
import dev.quarris.ppfluids.items.FluidRetrievalModuleItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.quarris.ppfluids.items.FluidExtractionModuleItem;
import dev.quarris.ppfluids.pipenetwork.FluidPipeItem;
import dev.quarris.ppfluids.pipe.FluidPipeBlock;
import dev.quarris.ppfluids.items.FluidItem;
import dev.quarris.ppfluids.pipe.FluidPipeTileEntity;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Locale;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContent {

    @ObjectHolder(PPFluids.ID+":fluid_pipe")
    public static Block FLUID_PIPE;

    @ObjectHolder(PPFluids.ID+":fluid_item")
    public static Item FLUID_ITEM;

    @ObjectHolder(PPFluids.ID+":fluid_pipe")
    public static TileEntityType<FluidPipeTileEntity> FLUID_PIPE_TILE;

    @ObjectHolder(PPFluids.ID+":fluid_filter")
    public static ContainerType<FluidFilterModuleContainer> FLUID_FILTER_CONTAINER;
    @ObjectHolder(PPFluids.ID+":fluid_extraction")
    public static ContainerType<FluidExtractionModuleContainer> FLUID_EXTRACTION_CONTAINER;
    @ObjectHolder(PPFluids.ID+":fluid_retrieval")
    public static ContainerType<FluidRetrievalModuleContainer> FLUID_RETRIEVAL_CONTAINER;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new FluidPipeBlock().setRegistryName(PPFluids.ID, "fluid_pipe")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new BlockItem(FLUID_PIPE, new Item.Properties().group(Registry.GROUP)).setRegistryName(FLUID_PIPE.getRegistryName()),
                new FluidItem().setRegistryName(PPFluids.ID, "fluid_item")
        );

        event.getRegistry().registerAll(createTieredModule("fluid_extraction_module", FluidExtractionModuleItem::new));
        event.getRegistry().registerAll(createTieredModule("fluid_filter_module", FluidFilterModuleItem::new));
        event.getRegistry().registerAll(createTieredModule("fluid_retrieval_module", FluidRetrievalModuleItem::new));
        IPipeItem.TYPES.put(FluidPipeItem.TYPE, FluidPipeItem::new);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(FluidPipeTileEntity::new, FLUID_PIPE).build(null).setRegistryName(FLUID_PIPE.getRegistryName())
        );
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                createPipeContainer(PPFluids.createRes("fluid_filter")),
                createPipeContainer(PPFluids.createRes("fluid_extraction")),
                createPipeContainer(PPFluids.createRes("fluid_retrieval"))
        );
    }

    private static Item[] createTieredModule(String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        Item[] items = new Item[ModuleTier.values().length];
        int i = 0;
        for(ModuleTier tier : ModuleTier.values()) {
            items[i] = item.apply(name, tier).setRegistryName(PPFluids.ID, tier.name().toLowerCase(Locale.ROOT) + "_" + name);
            i++;
        }
        return items;
    }

    private static <T extends AbstractPipeContainer<?>> ContainerType<T> createPipeContainer(ResourceLocation name) {
        return (ContainerType<T>) IForgeContainerType.create((windowId, inv, data) -> {
            PipeTileEntity tile = Utility.getTileEntity(PipeTileEntity.class, inv.player.world, data.readBlockPos());
            int moduleIndex = data.readInt();
            ItemStack moduleStack = tile.modules.getStackInSlot(moduleIndex);
            return ((IModule)moduleStack.getItem()).getContainer(moduleStack, tile, windowId, inv, inv.player, moduleIndex);
        }).setRegistryName(name);
    }
}
