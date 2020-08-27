package quarris.ppfluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PPFluids.ID)
public class PPFluids {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String ID = "ppfluids";

    public static ResourceLocation createRes(String name) {
        return new ResourceLocation(ID, name);
    }

    public PPFluids() {

    }
}
