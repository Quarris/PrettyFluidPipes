package dev.quarris.ppfluids.client.eventhandler;

import de.ellpeck.prettypipes.pipe.PipeRenderer;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.client.screen.FluidExtractionScreen;
import dev.quarris.ppfluids.client.screen.FluidFilterScreen;
import dev.quarris.ppfluids.client.screen.FluidLimiterScreen;
import dev.quarris.ppfluids.client.screen.FluidRetrievalScreen;
import dev.quarris.ppfluids.registry.BlockEntitySetup;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = ModRef.ID, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {

    @SubscribeEvent
    public static void registerMenus(RegisterMenuScreensEvent event) {
        event.register(MenuSetup.FLUID_FILTER.get(), FluidFilterScreen::new);
        event.register(MenuSetup.FLUID_EXTRACTION.get(), FluidExtractionScreen::new);
        event.register(MenuSetup.FLUID_RETRIEVAL.get(), FluidRetrievalScreen::new);
        event.register(MenuSetup.FLUID_LIMITER.get(), FluidLimiterScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitySetup.FLUID_PIPE.get(), PipeRenderer::new);
    }
}
