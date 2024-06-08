package betterquesting.api.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public interface IPacketSender {
    // Server to Client
    void sendToPlayers(QuestingPacket payload, ServerPlayer... players);

    void sendToAll(QuestingPacket payload);

    // Client to Server
    void sendToServer(QuestingPacket payload);

    // Misc.
    void sendToAround(QuestingPacket payload, TargetPoint point);

    void sendToDimension(QuestingPacket payload, int dimension);
}
