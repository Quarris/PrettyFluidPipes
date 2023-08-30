package dev.quarris.ppfluids.datagen.server;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.quarris.ppfluids.registry.BlockSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableGen extends LootTableProvider {

    public LootTableGen(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {

    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
            Pair.of(BlockLootProvider::new, LootContextParamSets.BLOCK)
        );
    }

    public static class BlockLootProvider extends BlockLoot {

        @Override
        protected void addTables() {
            this.dropSelf(BlockSetup.FLUID_PIPE.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockSetup.REGISTRY.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
        }
    }
}
