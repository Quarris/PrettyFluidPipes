package dev.quarris.ppfluids.network;

import dev.quarris.ppfluids.PPFluids;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {
    private static final String VERSION = "1";
    private static SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            PPFluids.createRes("channel"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    public static void init() {
        channel.registerMessage(0, ButtonPacket.class, ButtonPacket::encode, ButtonPacket::decode, ButtonPacket::handle);
    }

    public static void sendToAllLoaded(World world, BlockPos pos, Object message) {
        channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), message);
    }

    public static void sendTo(PlayerEntity player, Object message) {
        channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }

    public static void sendToServer(Object message) {
        channel.send(PacketDistributor.SERVER.noArg(), message);
    }
}