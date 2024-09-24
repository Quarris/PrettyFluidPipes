package dev.quarris.ppfluids.datagen.server;

import dev.quarris.ppfluids.registry.BlockSetup;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootTableGen extends LootTableProvider {

    public LootTableGen(PackOutput pOutput, Set<ResourceKey<LootTable>> pRequiredTables, List<SubProviderEntry> pSubProviders, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRequiredTables, pSubProviders, pRegistries);
    }

    public static class BlockLoot extends BlockLootSubProvider {

        public BlockLoot(HolderLookup.Provider lookupProvider) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
        }

        @Override
        protected void generate() {
            this.dropSelf(BlockSetup.FLUID_PIPE.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockSetup.REGISTRY.getEntries().stream().map(holder -> ((Block) holder.get()))::iterator;
        }
    }
}
