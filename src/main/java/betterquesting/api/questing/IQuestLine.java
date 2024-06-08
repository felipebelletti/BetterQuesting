package betterquesting.api.questing;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.CompoundTag;

public interface IQuestLine extends IDatabase<IQuestLineEntry>, INBTPartial<CompoundTag, Integer>, IPropertyContainer {
    IQuestLineEntry createNew(int id);

    String getUnlocalisedName();

    String getUnlocalisedDescription();

    DBEntry<IQuestLineEntry> getEntryAt(int x, int y);
}
