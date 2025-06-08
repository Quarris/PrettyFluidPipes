package dev.quarris.ppfluids.datagen.server;

import de.ellpeck.prettypipes.PrettyPipes;
import de.ellpeck.prettypipes.Registry;
import de.ellpeck.prettypipes.items.ModuleTier;
import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.ItemSetup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class RecipesGen extends RecipeProvider {

    public RecipesGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider registries) {
        blocks(output);
        modules(output);
    }

    private static void blocks(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BlockSetup.FLUID_PIPE)
            .requires(Registry.pipeBlock)
            .group("pipe")
            .unlockedBy("has_pipe", has(Registry.pipeBlock))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registry.pipeBlock)
            .requires(BlockSetup.FLUID_PIPE)
            .group("pipe")
            .unlockedBy("has_pipe", has(BlockSetup.FLUID_PIPE))
            .save(output, ModRef.res("fluid_pipe_to_pipe"));
    }

    private static void modules(RecipeOutput output) {
        // Extraction Modules
        Item blankModule = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(PrettyPipes.ID, "blank_module"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.EXTRACTION_MODULES.get(ModuleTier.LOW))
            .pattern(" P ")
            .pattern("LML")
            .pattern(" L ")
            .define('P', Blocks.PISTON)
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('M', blankModule)
            .unlockedBy("has_piston", has(Blocks.PISTON))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.EXTRACTION_MODULES.get(ModuleTier.MEDIUM))
            .pattern(" I ")
            .pattern("IMI")
            .pattern(" I ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('M', ItemSetup.EXTRACTION_MODULES.get(ModuleTier.LOW))
            .unlockedBy("has_module", has(ItemSetup.EXTRACTION_MODULES.get(ModuleTier.LOW)))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.EXTRACTION_MODULES.get(ModuleTier.HIGH))
            .pattern("GDG")
            .pattern("GMG")
            .pattern("GGG")
            .define('D', Tags.Items.GEMS_DIAMOND)
            .define('G', Tags.Items.INGOTS_GOLD)
            .define('M', ItemSetup.EXTRACTION_MODULES.get(ModuleTier.MEDIUM))
            .unlockedBy("has_module", has(ItemSetup.EXTRACTION_MODULES.get(ModuleTier.MEDIUM)))
            .save(output);

        // Filter Modules
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.FILTER_MODULES.get(ModuleTier.LOW))
            .pattern(" H ")
            .pattern("LML")
            .pattern(" L ")
            .define('H', Blocks.HOPPER)
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('M', blankModule)
            .unlockedBy("has_piston", has(Blocks.PISTON))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.FILTER_MODULES.get(ModuleTier.MEDIUM))
            .pattern(" B ")
            .pattern("IMI")
            .pattern(" B ")
            .define('B', Blocks.IRON_BARS)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('M', ItemSetup.FILTER_MODULES.get(ModuleTier.LOW))
            .unlockedBy("has_module", has(ItemSetup.FILTER_MODULES.get(ModuleTier.LOW)))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.FILTER_MODULES.get(ModuleTier.HIGH))
            .pattern("GBG")
            .pattern("BMB")
            .pattern("GBG")
            .define('B', Blocks.IRON_BARS)
            .define('G', Tags.Items.INGOTS_GOLD)
            .define('M', ItemSetup.FILTER_MODULES.get(ModuleTier.MEDIUM))
            .unlockedBy("has_module", has(ItemSetup.FILTER_MODULES.get(ModuleTier.MEDIUM)))
            .save(output);

        // Retrieval Modules
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.LOW))
            .pattern(" P ")
            .pattern("LML")
            .pattern(" E ")
            .define('P', Blocks.STICKY_PISTON)
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('E', Tags.Items.ENDER_PEARLS)
            .define('M', blankModule)
            .unlockedBy("has_piston", has(Blocks.PISTON))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.MEDIUM))
            .pattern("LLL")
            .pattern("IMI")
            .pattern(" I ")
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('M', ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.LOW))
            .unlockedBy("has_module", has(ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.LOW)))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.HIGH))
            .pattern("LDL")
            .pattern("GMG")
            .pattern("LGL")
            .define('D', Tags.Items.GEMS_DIAMOND)
            .define('G', Tags.Items.INGOTS_GOLD)
            .define('L', Tags.Items.STORAGE_BLOCKS_LAPIS)
            .define('M', ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.MEDIUM))
            .unlockedBy("has_module", has(ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.MEDIUM)))
            .save(output);

        // Limiter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ItemSetup.LIMITER_MODULE)
            .pattern(" D ")
            .pattern("LML")
            .pattern(" L ")
            .define('D', Blocks.DROPPER)
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('M', blankModule)
            .unlockedBy("has_module", has(ItemSetup.RETRIEVAL_MODULES.get(ModuleTier.MEDIUM)))
            .save(output);

    }
}
