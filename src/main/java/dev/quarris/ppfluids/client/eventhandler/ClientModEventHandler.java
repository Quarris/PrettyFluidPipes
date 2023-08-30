package dev.quarris.ppfluids.client.eventhandler;

import de.ellpeck.prettypipes.pipe.PipeRenderer;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.client.screen.FluidExtractionModuleScreen;
import dev.quarris.ppfluids.client.screen.FluidFilterModuleScreen;
import dev.quarris.ppfluids.client.screen.FluidRetrievalModuleScreen;
import dev.quarris.ppfluids.registry.BlockEntitySetup;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ModRef.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(BlockSetup.FLUID_PIPE.get(), RenderType.cutout());
        MenuScreens.register(MenuSetup.FLUID_FILTER_CONTAINER.get(), FluidFilterModuleScreen::new);
        MenuScreens.register(MenuSetup.FLUID_EXTRACTION_CONTAINER.get(), FluidExtractionModuleScreen::new);
        MenuScreens.register(MenuSetup.FLUID_RETRIEVAL_CONTAINER.get(), FluidRetrievalModuleScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitySetup.FLUID_PIPE.get(), PipeRenderer::new);
    }
}
