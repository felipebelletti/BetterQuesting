package betterquesting.api.placeholders.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TaskPlaceholder implements ITask {
    private CompoundTag nbtData = new CompoundTag();

    public void setTaskConfigData(CompoundTag nbt) {
        nbtData.setTag("orig_data", nbt);
    }

    public void setTaskProgressData(CompoundTag nbt) {
        nbtData.setTag("orig_prog", nbt);
    }

    public CompoundTag getTaskConfigData() {
        return nbtData.getCompoundTag("orig_data");
    }

    public CompoundTag getTaskProgressData() {
        return nbtData.getCompoundTag("orig_prog");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.setTag("orig_data", nbtData.getCompoundTag("orig_data"));
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        nbtData.setTag("orig_data", nbt.getCompoundTag("orig_data"));
    }

    @Override
    public CompoundTag writeProgressToNBT(CompoundTag nbt, @Nullable List<UUID> users) {
        nbt.setTag("orig_prog", nbtData.getCompoundTag("orig_prog"));
        return nbt;
    }

    @Override
    public void readProgressFromNBT(CompoundTag nbt, boolean merge) {
        nbtData.setTag("orig_prog", nbt.getCompoundTag("orig_prog"));
    }

    @Override
    public String getUnlocalisedName() {
        return "betterquesting.placeholder";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskPlaceholder.INSTANCE.getRegistryName();
    }

    @Override
    public void detect(ParticipantInfo participant, DBEntry<IQuest> quest) {
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return false;
    }

    @Override
    public void setComplete(UUID uuid) {
    }

    @Override
    public void resetUser(UUID uuid) {
    }

    @Override
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return null;
    }

    @Override
    public Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest) {
        return null;
    }
}
