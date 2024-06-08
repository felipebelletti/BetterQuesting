package betterquesting.api.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public interface IPacketSender {
    // Server to Client
    void sendToPlayers(QuestingPacket payload, ServerPlayer... players);

    void sendToAll(QuestingPacket payload);

    // Client to Server
    void sendToServer(QuestingPacket payload);

    // Misc.
    void sendToAround(QuestingPacket payload, PacketDistributor.TargetPoint point);

    void sendToDimension(QuestingPacket payload, int dimension);
}
