package quarris.ppfluids;

import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.items.ModuleItem;
import de.ellpeck.prettypipes.items.ModuleTier;
import de.ellpeck.prettypipes.pipe.IPipeItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarris.ppfluids.items.FluidExtractionModuleItem;
import quarris.ppfluids.network.FluidPipeItem;
import quarris.ppfluids.pipe.FluidPipeBlock;
import quarris.ppfluids.items.FluidItem;
import quarris.ppfluids.pipe.FluidPipeTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContent {

    public static final Block FLUID_PIPE = new FluidPipeBlock();

    public static final Item FLUID_ITEM = new FluidItem();

    public static final TileEntityType<FluidPipeTileEntity> FLUID_PIPE_TILE = TileEntityType.Builder.create(FluidPipeTileEntity::new, FLUID_PIPE).build(null);

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                FLUID_PIPE.setRegistryName(PPFluids.ID, "fluid_pipe")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new BlockItem(FLUID_PIPE, new Item.Properties().group(Registry.GROUP)).setRegistryName(FLUID_PIPE.getRegistryName()),
                FLUID_ITEM.setRegistryName(PPFluids.ID, "fluid_item")
        );

        event.getRegistry().registerAll(createTieredModule("fluid_extraction_module", FluidExtractionModuleItem::new));
        IPipeItem.TYPES.put(FluidPipeItem.TYPE, FluidPipeItem::new);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(
                FLUID_PIPE_TILE.setRegistryName(FLUID_PIPE.getRegistryName())
        );
    }

    private static Item[] createTieredModule(String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        List<Item> items = new ArrayList();
        ModuleTier[] tiers = ModuleTier.values();
        int size = tiers.length;

        for(int i = 0; i < size; ++i) {
            ModuleTier tier = tiers[i];
            items.add((item.apply(name, tier)).setRegistryName(PPFluids.ID, tier.name().toLowerCase(Locale.ROOT) + "_" + name));
        }

        return items.toArray(new Item[0]);
    }
}
