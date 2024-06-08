package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class NetBulkSync // Clears local data and negotiates a full resync with the server
{
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "main_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetBulkSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetBulkSync::onClient);
        }
    }

    public static void sendReset(@Nullable ServerPlayer player, boolean reset, boolean respond) {
        CompoundTag payload = new CompoundTag();
        payload.setBoolean("reset", reset);
        payload.setBoolean("respond", respond);

        if (player == null) // Don't use this on a large server unless absolutely necessary!
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        }
    }

    public static void sendSync(@Nonnull ServerPlayer player) {
        boolean nameChanged = NameCache.INSTANCE.updateName(player);
        UUID playerID = QuestingAPI.getQuestingUUID(player);

        NetSettingSync.sendSync(player);
        NetQuestSync.sendSync(player, null, true, true);
        NetChapterSync.sendSync(player, null);
        NetLifeSync.sendSync(new ServerPlayer[]{player}, new UUID[]{playerID});
        DBEntry<IParty> party = PartyManager.INSTANCE.getParty(playerID);
        List<Entry<Integer, Long>> invites = PartyInvitations.INSTANCE.getPartyInvites(playerID);
        int partyCount = invites.size() + (party == null ? 0 : 1);
        if (partyCount > 0) {
            int[] pids = new int[partyCount];
            for (int i = 0; i < invites.size(); i++) {
                pids[i] = invites.get(i).getKey();
            }
            if (party != null) pids[partyCount - 1] = party.getID();
            NetPartySync.sendSync(new ServerPlayer[]{player}, pids);
        }
        if (party != null) {
            NetNameSync.quickSync(nameChanged ? null : player, party.getID());
        } else {
            NetNameSync.sendNames(new ServerPlayer[]{player}, new UUID[]{playerID}, null);
        }
        NetInviteSync.sendSync(player);
        NetCacheSync.sendSync(player);
    }

    private static void onServer(Tuple<CompoundTag, ServerPlayer> message) {
        sendSync(message.getSecond()); // Can include more sync options at a later date
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        if (message.getBoolean("reset") && !Minecraft.getInstance().isIntegratedServerRunning()) // DON'T do this on LAN hosts
        {
            SaveLoadHandler.INSTANCE.unloadDatabases();
        }

        if (message.getBoolean("respond")) // Client doesn't really have to honour this but it would mess with things otherwise
        {
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, new CompoundTag()));
        }
    }
}
