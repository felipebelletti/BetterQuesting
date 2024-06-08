package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyInvitations;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.UUID;

public class NetInviteSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "invite_sync");

    public static void registerHandler() {
        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetInviteSync::onClient);
        }
    }

    // If I need to send other people's invites to players then I'll deal with that another time
    public static void sendSync(@Nonnull ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        payload.setInteger("action", 0);
        payload.setTag("data", PartyInvitations.INSTANCE.writeToNBT(new ListTag(), Collections.singletonList(playerID)));
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }

    public static void sendRevoked(@Nonnull ServerPlayer player, int... IDs) {
        CompoundTag payload = new CompoundTag();
        payload.setInteger("action", 1);
        payload.setIntArray("IDs", IDs);
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        int action = message.getInteger("action");
        if (action == 0) {
            PartyInvitations.INSTANCE.readFromNBT(message.getTagList("data", 10), true);
            MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
        } else if (action == 1) {
            UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getInstance().player);
            PartyInvitations.INSTANCE.revokeInvites(playerID, message.getIntArray("IDs"));
            MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
        }
    }
}
