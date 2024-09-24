package dev.quarris.ppfluids;

import dev.quarris.ppfluids.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(ModRef.ID)
public class PrettyPipesFluids {

    public PrettyPipesFluids(IEventBus modBus, ModContainer modContainer) {
        BlockSetup.init(modBus);
        ItemSetup.init(modBus);
        BlockEntitySetup.init(modBus);
        MenuSetup.init(modBus);
        DataComponentSetup.init(modBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ModConfigs.register(new ModConfigSpec.Builder()));
    }
}
