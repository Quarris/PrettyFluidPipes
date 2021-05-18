package dev.quarris.ppfluids;

import de.ellpeck.prettypipes.pipe.PipeRenderer;
import dev.quarris.ppfluids.client.FluidExtractionModuleScreen;
import dev.quarris.ppfluids.client.FluidFilterModuleScreen;
import dev.quarris.ppfluids.client.FluidRetrievalModuleScreen;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import dev.quarris.ppfluids.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
            RenderTypeLookup.setRenderLayer(ModContent.FLUID_PIPE, RenderType.getCutout());
            ClientRegistry.bindTileEntityRenderer(ModContent.FLUID_PIPE_TILE, PipeRenderer::new);
            ScreenManager.registerFactory(ModContent.FLUID_FILTER_CONTAINER, FluidFilterModuleScreen::new);
            ScreenManager.registerFactory(ModContent.FLUID_EXTRACTION_CONTAINER, FluidExtractionModuleScreen::new);
            ScreenManager.registerFactory(ModContent.FLUID_RETRIEVAL_CONTAINER, FluidRetrievalModuleScreen::new);
        }
    }
}
