package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskScoreboard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class FactoryTaskScoreboard implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskScoreboard INSTANCE = new FactoryTaskScoreboard();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":scoreboard");
    }

    @Override
    public TaskScoreboard createNew() {
        return new TaskScoreboard();
    }

    @Override
    public TaskScoreboard loadFromData(CompoundTag json) {
        TaskScoreboard task = new TaskScoreboard();
        task.readFromNBT(json);
        return task;
    }

}
