package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.loot.LootRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class NetLootSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:loot_database");

    public static void registerHandler() {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetLootSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            QuestingAPI.getAPI(ApiReference.PACKET_REG).registerClientHandler(ID_NAME, NetLootSync::onClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void requestEdit(CompoundTag data) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", data);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    public static void sendSync(@Nullable ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", LootRegistry.INSTANCE.writeToNBT(new CompoundTag(), null));

        if (player == null) {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToAll(new QuestingPacket(ID_NAME, payload));
        } else {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        }
    }

    private static void onServer(Tuple<CompoundTag, ServerPlayer> message) {
        ServerPlayer sender = message.getSecond();
        CompoundTag data = message.getFirst();

        if (sender.getServer() == null) return;
        if (!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile())) {
            BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit loot chests without OP permissions!");
            sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"), true);
            return; // Player is not operator. Do nothing
        }

        BetterQuesting.logger.log(Level.INFO, "Player " + sender.getName() + " edited loot chests");

        LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("data"), false);
        sendSync(null);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        LootRegistry.INSTANCE.readFromNBT(message.getCompoundTag("data"), false);
        LootRegistry.INSTANCE.updateUI = true;
    }
}
