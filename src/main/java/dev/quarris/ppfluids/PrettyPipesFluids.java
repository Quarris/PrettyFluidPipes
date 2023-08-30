package dev.quarris.ppfluids;

import dev.quarris.ppfluids.registry.BlockEntitySetup;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.ItemSetup;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModRef.ID)
public class PrettyPipesFluids {

    public PrettyPipesFluids() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockSetup.init(modBus);
        ItemSetup.init(modBus);
        BlockEntitySetup.init(modBus);
        MenuSetup.init(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.register(new ForgeConfigSpec.Builder()));
    }
}
