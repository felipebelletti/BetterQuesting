package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Ignore the invite system here. We'll deal wih that elsewhere
public class NetPartySync {
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "party_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetPartySync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetPartySync::onClient);
        }
    }

    public static void quickSync(int partyID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        IParty party = PartyManager.INSTANCE.getValue(partyID);

        if (server == null || party == null) return;

        List<ServerPlayer> players = new ArrayList<>();
        for (UUID uuid : party.getMembers()) {
            ServerPlayer p = server.getPlayerList().getPlayerByUUID(uuid);
            //noinspection ConstantConditions
            if (p != null) players.add(p);
        }

        sendSync(players.toArray(new ServerPlayer[0]), new int[]{partyID});
    }

    public static void sendSync(@Nullable ServerPlayer[] players, @Nullable int[] partyIDs) {
        if (partyIDs != null && partyIDs.length <= 0) return;
        if (players != null && players.length <= 0) return;

        ListTag dataList = new ListTag();
        final List<DBEntry<IParty>> partySubset = partyIDs == null ? PartyManager.INSTANCE.getEntries() : PartyManager.INSTANCE.bulkLookup(partyIDs);
        for (DBEntry<IParty> party : partySubset) {
            CompoundTag entry = new CompoundTag();
            entry.setInteger("partyID", party.getID());
            entry.setTag("config", party.getValue().writeToNBT(new CompoundTag()));
            dataList.appendTag(entry);
        }

        CompoundTag payload = new CompoundTag();
        payload.setTag("data", dataList);
        payload.setBoolean("merge", partyIDs != null);

        if (players == null) {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void requestSync(@Nullable int[] partyIDs) {
        CompoundTag payload = new CompoundTag();
        if (partyIDs != null) payload.setIntArray("partyIDs", partyIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    private static void onServer(Tuple<CompoundTag, ServerPlayer> message) {
        CompoundTag payload = message.getFirst();
        int[] reqIDs = !payload.hasKey("partyIDs") ? null : payload.getIntArray("partyIDs");
        sendSync(new ServerPlayer[]{message.getSecond()}, reqIDs);
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(CompoundTag message) {
        ListTag data = message.getTagList("data", 10);
        if (!message.getBoolean("merge")) PartyManager.INSTANCE.reset();

        for (int i = 0; i < data.tagCount(); i++) {
            CompoundTag tag = data.getCompoundTagAt(i);
            if (!tag.hasKey("partyID", 99)) continue;
            int partyID = tag.getInteger("partyID");

            IParty party = PartyManager.INSTANCE.getValue(partyID); // TODO: Send to client side database
            if (party == null) party = PartyManager.INSTANCE.createNew(partyID);

            party.readFromNBT(tag.getCompoundTag("config"));
        }

        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    }
}
