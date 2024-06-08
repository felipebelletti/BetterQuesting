package betterquesting.api.questing;

import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.ListTag;

public interface IQuestDatabase extends IDatabase<IQuest>, INBTPartial<ListTag, Integer>, INBTProgress<ListTag> {
    IQuest createNew(int id);
}
