package quarris.ppfluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quarris.ppfluids.client.ClientSetup;

@Mod(PPFluids.ID)
public class PPFluids {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String ID = "ppfluids";

    public static ResourceLocation createRes(String name) {
        return new ResourceLocation(ID, name);
    }

    public PPFluids() {
        DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::setup);
            return null;
        });
    }
}
