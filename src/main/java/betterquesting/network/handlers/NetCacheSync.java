package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class NetCacheSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "cache_sync");

    public static void registerHandler() {
        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetCacheSync::onClient);
        }
    }

    public static void sendSync(@Nonnull ServerPlayer player) {
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if (qc == null) return;
        CompoundTag payload = new CompoundTag();
        payload.setTag("data", qc.serializeNBT());
        PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(CompoundTag message) {
        Player player = Minecraft.getMinecraft().player;
        QuestCache qc = player != null ? player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null) : null;
        if (qc != null) qc.deserializeNBT(message.getCompoundTag("data"));
    }
}
