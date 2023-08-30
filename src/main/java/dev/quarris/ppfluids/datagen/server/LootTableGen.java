package dev.quarris.ppfluids.datagen.server;

import dev.quarris.ppfluids.registry.BlockSetup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LootTableGen extends LootTableProvider {

    public LootTableGen(PackOutput pOutput, Set<ResourceLocation> pRequiredTables, List<SubProviderEntry> pSubProviders) {
        super(pOutput, pRequiredTables, pSubProviders);
    }

    public static class BlockLoot extends BlockLootSubProvider {

        public BlockLoot() {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            this.dropSelf(BlockSetup.FLUID_PIPE.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockSetup.REGISTRY.getEntries().stream().flatMap(RegistryObject::stream)::iterator;
        }
    }
}
