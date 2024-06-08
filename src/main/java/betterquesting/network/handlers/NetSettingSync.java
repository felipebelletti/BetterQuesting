package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.storage.QuestSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;

public class NetSettingSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "setting_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetSettingSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetSettingSync::onClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void requestEdit() {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", QuestSettings.INSTANCE.writeToNBT(new CompoundTag()));
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    public static void sendSync(@Nullable ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", QuestSettings.INSTANCE.writeToNBT(new CompoundTag()));
        if (player != null) {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        } else {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        QuestSettings.INSTANCE.readFromNBT(message.getCompoundTag("data"));
    }

    private static void onServer(Tuple<CompoundTag, ServerPlayer> message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (!server.getPlayerList().canSendCommands(message.getSecond().getGameProfile())) {
            BetterQuesting.logger.log(Level.WARN, "Player " + message.getSecond().getName() + " (UUID:" + QuestingAPI.getQuestingUUID(message.getSecond()) + ") tried to edit settings without OP permissions!");
            sendSync(message.getSecond());
            return;
        }

        QuestSettings.INSTANCE.readFromNBT(message.getFirst().getCompoundTag("data"));
        SaveLoadHandler.INSTANCE.markDirty();
        sendSync(null);
    }
}
