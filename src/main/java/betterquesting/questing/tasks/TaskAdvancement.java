package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.editors.tasks.GuiEditTaskAdvancement;
import betterquesting.client.gui2.tasks.PanelTaskAdvancement;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

public class TaskAdvancement implements ITask {
    private final Set<UUID> completeUsers = new TreeSet<>();
    public ResourceLocation advID;

    @Override
    public String getUnlocalisedName() {
        return "bq_standard.task.advancement";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskAdvancement.INSTANCE.getRegistryName();
    }

    public void onAdvancementGet(DBEntry<IQuest> quest, ParticipantInfo pInfo, Advancement advancement) {
        if (advancement == null || advID == null || !advID.equals(advancement.getId())) return;
        detect(pInfo, quest);
    }

    @Override
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
        if (!(pInfo.PLAYER instanceof ServerPlayer) || pInfo.PLAYER.getServer() == null || advID == null) return;

        Advancement adv = pInfo.PLAYER.getServer().getAdvancementManager().getAdvancement(advID);
        if (adv == null) return;
        PlayerAdvancements playerAdv = pInfo.PLAYER.getServer().getPlayerList().getPlayerAdvancements((ServerPlayer) pInfo.PLAYER);

        if (playerAdv.getProgress(adv).isDone()) setComplete(pInfo.UUID);
        pInfo.markDirty(Collections.singletonList(quest.getID()));
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskAdvancement(rect, this);
    }

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest) {
        return new GuiEditTaskAdvancement(parent, quest, this);
    }

    @Override
    public CompoundTag writeProgressToNBT(CompoundTag nbt, @Nullable List<UUID> users) {
        ListTag jArray = new ListTag();

        completeUsers.forEach((uuid) -> {
            if (users == null || users.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
        });

        nbt.setTag("completeUsers", jArray);

        return nbt;
    }

    @Override
    public void readProgressFromNBT(CompoundTag nbt, boolean merge) {
        if (!merge) completeUsers.clear();
        ListTag cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setString("advancement_id", advID == null ? "" : advID.toString());
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        String id = nbt.getString("advancement_id");
        advID = StringUtils.isNullOrEmpty(id) ? null : new ResourceLocation(id);
    }

    @Override
    public List<String> getTextForSearch() {
        return Collections.singletonList(advID.toString());
    }
}
