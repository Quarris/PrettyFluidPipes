package dev.quarris.ppfluids.datagen;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.datagen.server.BlockTagGen;
import dev.quarris.ppfluids.datagen.server.LootTableGen;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRef.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenEvents {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        data.addProvider(event.includeServer(), new BlockTagGen(data, fileHelper));
        data.addProvider(event.includeServer(), new LootTableGen(data));
    }


}
