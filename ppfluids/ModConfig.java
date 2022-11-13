package old.dev.quarris.ppfluids;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {

    public static ForgeConfigSpec.BooleanValue dropFluidContainers;

    public static ForgeConfigSpec register(ForgeConfigSpec.Builder builder) {
        dropFluidContainers = builder.comment(
                "Set to true if you want the fluid to drop fluid containers when there is no way to store the fluid in the network.",
                "Enabling this might cause high number of item entities to spawn accidentally by users."
        ).define("dropFluid", false);
        return builder.build();
    }
}
