package dev.quarris.ppfluids.registry;

import de.ellpeck.prettypipes.Registry;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.network.FluidButtonPayload;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = ModRef.ID, bus = EventBusSubscriber.Bus.MOD)
public class RegistryEvents {

    // Yes, this is Java so the names have to be this verbose.
    @SubscribeEvent
    public static void registerPrettyPipesFluidsItemsToPrettyPipesCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().toString().equalsIgnoreCase("prettypipes:tab")) {
            ItemSetup.REGISTRY.getEntries().forEach(entry -> {
                Item item = entry.get();
                if (item == ItemSetup.FLUID_HOLDER.get()) {
                    return;
                }

                event.accept(item);
            });
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ModRef.ID);
        registrar.playBidirectional(FluidButtonPayload.TYPE, FluidButtonPayload.CODEC, FluidButtonPayload::onMessage);
    }

    @SubscribeEvent
    public static void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidHandlerItemStack.Consumable(DataComponentSetup.FLUID_CONTENT_DATA, stack, Integer.MAX_VALUE), ItemSetup.FLUID_HOLDER.get());
        event.registerBlockEntity(Registry.pipeConnectableCapability, BlockEntitySetup.FLUID_PIPE.get(), (e, d) -> e);
    }
}
