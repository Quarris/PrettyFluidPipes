package quarris.ppfluids;

import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.network.PipeNetwork;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarris.ppfluids.network.FluidPipeItem;

import java.util.List;

@Mod.EventBusSubscriber
public class EventHandler {

    /*
    @SubscribeEvent
    public static void updatePipeItems(WorldEvent.Load event) {
        if (event.getWorld() instanceof World) {
            PipeNetwork network = PipeNetwork.get((World) event.getWorld());
            for (BlockPos pos : network.getAllPipeLocations()) {
                List<PipeItem> pipeItems = network.getItemsInPipe(pos);
                for (int i = 0; i < pipeItems.size(); i++) {
                    PipeItem pipeItem = pipeItems.get(i);
                    if (!(pipeItem instanceof FluidPipeItem) && pipeItem.stack.getItem() == ModContent.FLUID_ITEM) {
                        pipeItems.set(i, new FluidPipeItem(pipeItem.serializeNBT()));
                    }
                }
            }
        }
    }
     */
}
