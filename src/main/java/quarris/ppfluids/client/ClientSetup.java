package quarris.ppfluids.client;

import de.ellpeck.prettypipes.pipe.PipeRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import quarris.ppfluids.ModContent;

public class ClientSetup {

    public static void setup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModContent.FLUID_PIPE, RenderType.cutout());

        ClientRegistry.bindTileEntityRenderer(ModContent.FLUID_PIPE_TILE, PipeRenderer::new);
    }

}
