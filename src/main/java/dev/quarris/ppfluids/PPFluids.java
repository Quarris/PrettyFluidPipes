package dev.quarris.ppfluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PPFluids.ID)
public class PPFluids {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String ID = "ppfluids";

    public static ResourceLocation createRes(String name) {
        return new ResourceLocation(ID, name);
    }

    public PPFluids() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, dev.quarris.ppfluids.ModConfig.register(new ForgeConfigSpec.Builder()));
    }
}
