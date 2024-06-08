package betterquesting.network.handlers;

import betterquesting.ScoreboardBQ;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;

public class NetScoreSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:score_sync");

    public static void registerHandler() {
        if (BetterQuesting.proxy.isClient()) {
            QuestingAPI.getAPI(ApiReference.PACKET_REG).registerClientHandler(ID_NAME, NetScoreSync::onClient);
        }
    }

    public static void sendScore(@Nullable ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", ScoreboardBQ.INSTANCE.writeToNBT(new ListTag(), player == null ? null : Collections.singletonList(QuestingAPI.getQuestingUUID(player))));
        payload.setBoolean("merge", player != null);

        if (player == null) {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToAll(new QuestingPacket(ID_NAME, payload));
        } else {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        }
    }

    private static void onClient(CompoundTag message) {
        ScoreboardBQ.INSTANCE.readFromNBT(message.getTagList("data", 10), message.getBoolean("merge"));
    }
}
