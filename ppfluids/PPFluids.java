package old.dev.quarris.ppfluids;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModRef.ID)
public class PPFluids {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String ID = "ppfluids";

    public PPFluids() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, old.dev.quarris.ppfluids.ModConfig.register(new ForgeConfigSpec.Builder()));

        ModContent.init(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
