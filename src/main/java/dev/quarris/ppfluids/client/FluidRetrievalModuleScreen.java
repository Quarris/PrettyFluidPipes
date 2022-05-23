package dev.quarris.ppfluids.client;

import de.ellpeck.prettypipes.pipe.containers.AbstractPipeGui;
import dev.quarris.ppfluids.container.FluidRetrievalModuleContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FluidRetrievalModuleScreen extends AbstractPipeGui<FluidRetrievalModuleContainer> {

    public FluidRetrievalModuleScreen(FluidRetrievalModuleContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    protected void init() {
        super.init();
        List<AbstractWidget> buttons = this.menu.getFilter().createScreenButtons(this, this.leftPos + 7, this.topPos + 17 + 32 + 18 * Mth.ceil((float)(this.menu).getFilter().size() / 9.0F) + 2);

        for (AbstractWidget button : buttons) {
            this.addRenderableWidget(button);
        }
    }
}
