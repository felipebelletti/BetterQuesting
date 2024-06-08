package betterquesting.questing.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskRetrieval;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskRetrieval implements IFactoryData<ITask, CompoundTag> {
    public static final FactoryTaskRetrieval INSTANCE = new FactoryTaskRetrieval();

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BetterQuesting.MODID_STD + ":retrieval");
    }

    @Override
    public TaskRetrieval createNew() {
        return new TaskRetrieval();
    }

    @Override
    public TaskRetrieval loadFromData(CompoundTag json) {
        TaskRetrieval task = new TaskRetrieval();
        task.readFromNBT(json);
        return task;
    }

}
