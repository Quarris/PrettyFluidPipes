package dev.quarris.ppfluids;

import dev.quarris.ppfluids.registry.BlockEntitySetup;
import dev.quarris.ppfluids.registry.BlockSetup;
import dev.quarris.ppfluids.registry.ItemSetup;
import dev.quarris.ppfluids.registry.MenuSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(ModRef.ID)
public class PrettyPipesFluids {

    public PrettyPipesFluids() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockSetup.init(modBus);
        ItemSetup.init(modBus);
        BlockEntitySetup.init(modBus);
        MenuSetup.init(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.register(new ModConfigSpec.Builder()));
    }
}
