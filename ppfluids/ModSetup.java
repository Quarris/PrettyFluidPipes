package old.dev.quarris.ppfluids;

import de.ellpeck.prettypipes.pipe.PipeRenderer;
import old.dev.quarris.ppfluids.client.FluidExtractionModuleScreen;
import old.dev.quarris.ppfluids.client.FluidFilterModuleScreen;
import old.dev.quarris.ppfluids.client.FluidRetrievalModuleScreen;
import old.dev.quarris.ppfluids.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PPFluids.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Client {

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            ItemBlockRenderTypes.setRenderLayer(ModContent.FLUID_PIPE.get(), RenderType.cutout());
            MenuScreens.register(ModContent.FLUID_FILTER_CONTAINER.get(), FluidFilterModuleScreen::new);
            MenuScreens.register(ModContent.FLUID_EXTRACTION_CONTAINER.get(), FluidExtractionModuleScreen::new);
            MenuScreens.register(ModContent.FLUID_RETRIEVAL_CONTAINER.get(), FluidRetrievalModuleScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModContent.FLUID_PIPE_TILE.get(), PipeRenderer::new);
        }
    }
}
