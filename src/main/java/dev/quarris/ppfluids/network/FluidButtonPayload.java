package dev.quarris.ppfluids.network;

import dev.quarris.ppfluids.ModRef;
import dev.quarris.ppfluids.misc.FluidFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.logging.log4j.util.TriConsumer;

public class FluidButtonPayload implements CustomPacketPayload {
    public static final ResourceLocation ID = ModRef.res("fluid_button");
    private BlockPos pos;
    private ButtonResult result;
    private int[] data;

    public FluidButtonPayload(BlockPos pos, ButtonResult result, int... data) {
        this.pos = pos;
        this.result = result;
        this.data = data;
    }


    public FluidButtonPayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.result = ButtonResult.values()[buf.readByte()];
        this.data = buf.readVarIntArray();
    }

    @Override
    public void write(FriendlyByteBuf pBuf) {
        pBuf.writeBlockPos(this.pos);
        pBuf.writeByte(this.result.ordinal());
        pBuf.writeVarIntArray(this.data);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void onMessage(final FluidButtonPayload message, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            Player player = ctx.player().orElseThrow();
            message.result.action.accept(message.pos, message.data, player);
        });
    }

    public static void sendAndExecute(BlockPos pos, ButtonResult result, int... data) {
        PacketDistributor.SERVER.noArg().send(new FluidButtonPayload(pos, result, data));
        result.action.accept(pos, data, Minecraft.getInstance().player);
    }

    public enum ButtonResult {
        FILTER_CHANGE((pos, data, player) -> {
            FluidFilter.IFluidFilteredContainer container = (FluidFilter.IFluidFilteredContainer)player.containerMenu;
            FluidFilter filter = container.getFilter();
            filter.onButtonPacket(container, data[0]);
        });

        public final TriConsumer<BlockPos, int[], Player> action;

        ButtonResult(TriConsumer<BlockPos, int[], Player> action) {
            this.action = action;
        }
    }
}
