package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.client.QuestNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ModReference;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;

public class NetNotices {
    // TODO: Convert over to inbox system in future
    private static final ResourceLocation ID_NAME = new ResourceLocation(ModReference.MODID, "notification");

    public static void registerHandler() {
        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetNotices::onClient);
        }
    }

    public static void sendNotice(@Nullable ServerPlayer[] players, ItemStack icon, String mainText, String subText, String sound) {
        CompoundTag payload = new CompoundTag();
        payload.setTag("icon", (icon != null ? icon : ItemStack.EMPTY).writeToNBT(new CompoundTag()));
        if (mainText != null) payload.setString("mainText", mainText);
        if (subText != null) payload.setString("subText", subText);
        if (sound != null) payload.setString("sound", sound);

        if (players != null) {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        } else {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundTag message) {
        ItemStack stack = new ItemStack(message.getCompoundTag("icon"));
        String mainTxt = message.getString("mainText");
        String subTxt = message.getString("subText");
        String sound = message.getString("sound");
        QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
    }
}
