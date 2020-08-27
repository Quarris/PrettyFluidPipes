package quarris.ppfluids.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import quarris.ppfluids.ModContent;

public class ClientSetup {

    public static void setup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModContent.FLUID_PIPE, RenderType.cutout());
    }

}
