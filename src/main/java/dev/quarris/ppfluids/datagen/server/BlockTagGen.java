package dev.quarris.ppfluids.datagen.server;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.registry.BlockSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTagGen extends BlockTagsProvider {


    public BlockTagGen(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, ModRef.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BlockSetup.FLUID_PIPE.get());
    }
}
