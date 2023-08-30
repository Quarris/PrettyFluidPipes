package dev.quarris.ppfluids.datagen;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.datagen.server.BlockTagGen;
import dev.quarris.ppfluids.datagen.server.LootTableGen;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ModRef.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenEvents {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        PackOutput output = data.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        data.addProvider(event.includeServer(), new BlockTagGen(output, lookup, fileHelper));
        data.addProvider(event.includeServer(), new LootTableGen(
            output,
            Collections.emptySet(),
            List.of(new LootTableProvider.SubProviderEntry(LootTableGen.BlockLoot::new, LootContextParamSets.BLOCK))
        ));
    }


}
