package old.dev.quarris.ppfluids;

import net.minecraft.resources.ResourceLocation;

public class ModRef {

    public static final String ID = "ppfluids";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(ID, name);
    }
}
