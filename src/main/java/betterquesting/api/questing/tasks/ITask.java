package betterquesting.api.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTProgress;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.api2.utils.ParticipantInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ITask extends INBTSaveLoad<CompoundTag>, INBTProgress<CompoundTag> {
    String getUnlocalisedName();

    ResourceLocation getFactoryID();

    void detect(ParticipantInfo participant, DBEntry<IQuest> quest);

    boolean isComplete(UUID uuid);

    void setComplete(UUID uuid);

    void resetUser(@Nullable UUID uuid);

    @Nullable
    @OnlyIn(Dist.CLIENT)
    IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest);

    @Nullable
    @OnlyIn(Dist.CLIENT)
    Screen getTaskEditor(Screen parent, DBEntry<IQuest> quest);

    /**
     * Tasks that set this to true will be ignored by quest completion logic.
     */
    default boolean ignored(UUID uuid) {
        return false;
    }

    default boolean displaysCenteredAlone() {
        return false;
    }

    default List<String> getTextForSearch() {
        return null;
    }
}
