package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.client.gui2.tasks.PanelTaskCheckbox;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskCheckbox;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class TaskCheckbox implements ITask {
    private final Set<UUID> completeUsers = new TreeSet<>();

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskCheckbox.INSTANCE.getRegistryName();
    }

    @Override
    public String getUnlocalisedName() {
        return BetterQuesting.MODID_STD + ".task.checkbox";
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

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
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
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskCheckbox(rect, quest, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public boolean displaysCenteredAlone() {
        return true;
    }
}
