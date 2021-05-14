package dev.quarris.ppfluids.network;

import dev.quarris.ppfluids.misc.FluidFilter;
import dev.quarris.ppfluids.misc.FluidFilter.IFluidFilteredContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Supplier;

public class ButtonPacket {
    private BlockPos pos;
    private ButtonResult result;
    private int[] data;

    public ButtonPacket(BlockPos pos, ButtonResult result, int... data) {
        this.pos = pos;
        this.result = result;
        this.data = data;
    }

    private ButtonPacket() {
    }

    public static ButtonPacket decode(PacketBuffer buf) {
        ButtonPacket packet = new ButtonPacket();
        packet.pos = buf.readBlockPos();
        packet.result = ButtonResult.values()[buf.readByte()];
        packet.data = buf.readVarIntArray();
        return packet;
    }

    public static void encode(ButtonPacket packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.result.ordinal());
        buf.writeVarIntArray(packet.data);
    }

    public static void handle(final ButtonPacket message, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            message.result.action.accept(message.pos, message.data, player);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendAndExecute(BlockPos pos, ButtonResult result, int... data) {
        PacketHandler.sendToServer(new ButtonPacket(pos, result, data));
        result.action.accept(pos, data, Minecraft.getInstance().player);
    }

    public static enum ButtonResult {
        FILTER_CHANGE((pos, data, player) -> {
            IFluidFilteredContainer container = (IFluidFilteredContainer)player.openContainer;
            FluidFilter filter = container.getFilter();
            filter.onButtonPacket(data[0]);
        });

        public final TriConsumer<BlockPos, int[], PlayerEntity> action;

        ButtonResult(TriConsumer<BlockPos, int[], PlayerEntity> action) {
            this.action = action;
        }
    }
}
