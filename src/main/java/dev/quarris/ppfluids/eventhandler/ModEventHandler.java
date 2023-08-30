package dev.quarris.ppfluids.eventhandler;

import de.ellpeck.prettypipes.Registry;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.network.PacketHandler;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.ItemSetup;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = ModRef.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    @SubscribeEvent
    public static void addItemsToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().toString().equalsIgnoreCase("prettypipes:tab")) {
            ItemSetup.REGISTRY.getEntries().forEach(entry -> {
                Item item = entry.get();
                if (item == ItemSetup.FLUID_HOLDER.get()) {
                    return;
                }

                event.accept(entry);
            });
        }
    }
}
