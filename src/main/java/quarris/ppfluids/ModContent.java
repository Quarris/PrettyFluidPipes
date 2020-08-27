package quarris.ppfluids;

import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.Registry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarris.ppfluids.blocks.FluidPipeBlock;

@Mod.EventBusSubscriber(modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContent {

    public static final Block FLUID_PIPE = new FluidPipeBlock();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                FLUID_PIPE.setRegistryName(PPFluids.createRes("fluid_pipe"))
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new BlockItem(FLUID_PIPE, new Item.Properties().group(Registry.GROUP)).setRegistryName(FLUID_PIPE.getRegistryName())
        );
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {

    }
}
