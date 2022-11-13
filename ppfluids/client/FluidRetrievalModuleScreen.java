package old.dev.quarris.ppfluids.client;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import old.dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidRetrievalModuleScreen extends AbstractPipeGui<FluidRetrievalModuleContainer> {

    public FluidRetrievalModuleScreen(FluidRetrievalModuleContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        for (AbstractWidget button : this.menu.getFilter().createButtons(this, this.leftPos + this.imageWidth - 7, this.topPos + 17 + 32 + 20)) {
            this.addRenderableWidget(button);
        }

        this.addRenderableWidget(this.menu.getSelector().getButton(this.leftPos + 7, this.topPos + 17 + 32 + 20));

    }
}
