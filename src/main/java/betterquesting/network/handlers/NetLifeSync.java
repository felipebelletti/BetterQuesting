package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.storage.LifeDatabase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class NetLifeSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "life_sync");

    public static void registerHandler() {
        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetLifeSync::onClient);
        }
    }

    public static void sendSync(@Nullable ServerPlayer[] players, @Nullable UUID[] playerIDs) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", LifeDatabase.INSTANCE.writeToNBT(new CompoundTag(), playerIDs == null ? null : Arrays.asList(playerIDs)));
        payload.setBoolean("merge", playerIDs != null);

        if (players != null) {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        } else {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        LifeDatabase.INSTANCE.readFromNBT(message.getCompoundTag("data"), message.getBoolean("merge"));
    }
}
